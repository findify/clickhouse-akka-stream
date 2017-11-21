package io.findify.clickhouse

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.{Attributes, Inlet, Materializer, SinkShape}
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, StageLogging}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.output.{JSONEachRowOutputFormat, OutputFormat}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

class ClickhouseSink(host: String, port: Int, table: String, format: OutputFormat = new JSONEachRowOutputFormat())
(
  implicit val system: ActorSystem,
  mat: Materializer
) extends GraphStageWithMaterializedValue[SinkShape[Seq[Row]], Future[Done]] with LazyLogging {
  val in: Inlet[Seq[Row]] = Inlet("input")
  override val shape: SinkShape[Seq[Row]] = SinkShape(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Done]) = {
    val promise = Promise[Done]
    val logic = new GraphStageLogic(shape) with StageLogging {
      val http = Http(system)
      import system.dispatcher

      override def preStart(): Unit = pull(in)

      val pullCallback = getAsyncCallback[Try[Done]] {
        case Success(_) =>
          logger.debug("flush complete, pulling")
          pull(in)
        case Failure(ex) =>
          logger.error("cannot flush", ex)
          promise.failure(ex)
          failStage(ex)
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val items = grab(in)
          flush(items).onComplete(pullCallback.invoke)
        }

        override def onUpstreamFinish(): Unit = {
          logger.info("stream completed")
          promise.success(Done)
          completeStage()
        }
      })
      def flush(inserts: Seq[Row]): Future[Done] = {
        val query = s"INSERT INTO $table FORMAT ${format.name}"
        logger.debug(s"query: $query")
        http.singleRequest(HttpRequest(
          method = HttpMethods.POST,
          uri = Uri(s"http://$host:$port/").withQuery(Query(Map("query" -> query))),
          entity = inserts.foldLeft(ByteString(""))( (bytes, row) => bytes ++ format.write(row)))
        ).flatMap {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String).map(line => {
              logger.info(s"status: OK, batch inserted, size=${inserts.size}")
              Done
            })
          case HttpResponse(code, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String).map(line => {
              logger.debug(s"query: $query")
              logger.debug(s"data: $inserts")
              logger.error(line)
              throw new IllegalArgumentException(s"non-200 ($code) response from clickhouse")
            })
        }

      }
    }
    (logic, promise.future)
  }
}
