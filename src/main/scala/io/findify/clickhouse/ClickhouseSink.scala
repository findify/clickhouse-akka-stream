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

case class ClickhouseSink(host: String, port: Int, table: String, format: OutputFormat = new JSONEachRowOutputFormat(), maxRowsInBuffer: Int = 2048)
(
  implicit val system: ActorSystem,
  mat: Materializer
) extends GraphStageWithMaterializedValue[SinkShape[Row], Future[Done]] with ClickhouseStream {
  val in: Inlet[Row] = Inlet("input")
  override val shape: SinkShape[Row] = SinkShape(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Done]) = {
    val promise = Promise[Done]
    val logic = new GraphStageLogic(shape) with StageLogging {
      val http = Http(system)
      val buffer = new FileBuffer[Nothing](table, format, maxRowsInBuffer)
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

    }
    (logic, promise.future)
  }
}
