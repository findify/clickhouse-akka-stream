package io.findify.clickhouse

import java.io.{File, FileInputStream, FileOutputStream}

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Keep, Sink, Source, StreamConverters}
import akka.stream._
import akka.stream.stage._
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.output.{JSONEachRowOutputFormat, OutputFormat}

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

class ClickhouseSink(host: String, port: Int, table: String, format: OutputFormat = new JSONEachRowOutputFormat(), maxRowsInBuffer: Int = 2048)
(
  implicit val system: ActorSystem,
  mat: Materializer
) extends GraphStageWithMaterializedValue[SinkShape[Row], Future[Done]] with LazyLogging {
  val in: Inlet[Row] = Inlet("input")
  override val shape: SinkShape[Row] = SinkShape(in)

  class FileBuffer {
    var tempFile = File.createTempFile("clickhouse", table)
    var tempStream = new FileOutputStream(tempFile)
    var size: Long = 0

    def append(row: Row): Unit = {
      val bytes = format.write(row).toArray
      tempStream.write(bytes)
      size += 1
    }

    def isFull: Boolean = size >= maxRowsInBuffer
    def stream: Source[ByteString, _] = {
      tempStream.close()
      StreamConverters.fromInputStream(() => new FileInputStream(tempFile))
    }
    def reset = {
      tempFile.delete()
      size = 0
      tempFile = File.createTempFile("clickhouse", table)
      tempStream = new FileOutputStream(tempFile)
    }

    def close = {
      tempStream.close()
      tempFile.delete()
    }
  }

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Done]) = {
    val promise = Promise[Done]
    val logic = new GraphStageLogic(shape) with StageLogging {
      val http = Http(system)
      val buffer = new FileBuffer()
      import system.dispatcher

      override def preStart(): Unit = pull(in)

      val pullCallback = getAsyncCallback[Try[Done]] {
        case Success(_) =>
          logger.debug("flush complete, pulling")
          buffer.reset
          pull(in)
        case Failure(ex) =>
          logger.error("cannot flush", ex)
          promise.failure(ex)
          buffer.close
          failStage(ex)
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val items = grab(in)
          buffer.append(items)
          if (buffer.isFull) {
            logger.debug(s"buffer is full (rows = ${buffer.size}), flushing")
            flush(buffer.stream).onComplete(pullCallback.invoke)
          } else {
            pull(in)
          }

        }

        override def onUpstreamFinish(): Unit = {
          logger.debug(s"upstream done, last flush (rows = ${buffer.size})")
          flush(buffer.stream).onComplete {
            case Success(_) =>
              logger.debug("last flush done, completing")
              promise.success(Done)
              buffer.close
              completeStage()
            case Failure(ex) =>
              logger.error("cannot do last flush", ex)
              promise.failure(ex)
              buffer.close
              failStage(ex)
          }
        }
      })

      def flush(stream: Source[ByteString, _]): Future[Done] = {
        val query = s"INSERT INTO $table FORMAT ${format.name}"
        logger.debug(s"query: $query")
        http.singleRequest(HttpRequest(
          method = HttpMethods.POST,
          uri = Uri(s"http://$host:$port/").withQuery(Query(Map("query" -> query))),
          entity = HttpEntity(ContentTypes.`application/json`, stream)
        )).flatMap {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String).map(line => {
              logger.info(s"status: OK, batch inserted: $line")
              Done
            })
          case HttpResponse(code, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String).map(line => {
              logger.debug(s"query: $query")
              logger.error(line)
              throw new IllegalArgumentException(s"non-200 ($code) response from clickhouse")
            })
        }

      }
    }
    (logic, promise.future)
  }
}
