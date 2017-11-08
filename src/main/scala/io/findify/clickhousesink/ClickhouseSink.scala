package io.findify.clickhousesink

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream._
import akka.stream.stage._
import akka.util.ByteString
import io.findify.clickhousesink.ClickhouseSink.Options
import io.findify.clickhousesink.encoder.Encoder

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import io.findify.clickhousesink.field._

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}
import scala.util.parsing.json.JSONArray

class ClickhouseSink[T]
(
  host: String,
  port: Int,
  table: String,
  options: ClickhouseSink.Options = Options()
)(
  implicit val system: ActorSystem,
  mat: Materializer,
  encoder: Encoder[T]
) extends GraphStageWithMaterializedValue[SinkShape[Seq[T]], Future[Done]] {
  val in: Inlet[Seq[T]] = Inlet("input")
  override val shape: SinkShape[Seq[T]] = SinkShape(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Done]) = {
    val promise = Promise[Done]
    val logic = new GraphStageLogic(shape) with StageLogging {
      val http = Http(system)
      import system.dispatcher

      override def preStart(): Unit = pull(in)

      val pullCallback = getAsyncCallback[Try[Done]] {
        case Success(_) =>
          log.debug("flush complete, pulling")
          pull(in)
        case Failure(ex) =>
          log.error(ex, "cannot flush")
          promise.failure(ex)
          failStage(ex)
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val items = grab(in)
          val blobs = items.map(item => ClickhouseSink.flatten(encoder.encode(item)))
          flush(blobs).onComplete(pullCallback.invoke)
        }

        override def onUpstreamFinish(): Unit = {
          log.info("done")
          promise.success(Done)
          completeStage()
        }
      })
      def flush(inserts: Seq[String]): Future[Done] = {
        val data = inserts.mkString(s"INSERT INTO $table values ", ",", "")
        log.info(data)
        http.singleRequest(HttpRequest(
          method = HttpMethods.POST,
          uri = Uri(s"http://$host:$port/"),
          entity = data)
        ).flatMap {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String).map(line => {
              log.info(line)
              Done
            })
          case HttpResponse(code, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String).map(line => {
              log.error(line)
              throw new IllegalArgumentException(s"non-200 ($code) response from clickhouse")
            })
        }

      }
    }
    (logic, promise.future)
  }
}
object ClickhouseSink {
  case class Options(mapper: CustomMapper = CustomMapper.Noop)

  def flatten(item: Seq[Field]): String = {
    val merged = item.map {
      case SimpleField(value) => value
      //case ArrayField(values) if values.isEmpty => "[]"
      case ArrayField(values) => values.mkString("[", ",", "]")
      case NestedTable(rows, _) if rows.nonEmpty =>
        rows.map(_.values).transpose.map(col => col.map {
          case SimpleField(value) => value
          case _ => ???
        }.mkString("[", ",", "]")).mkString(",")
      case NestedTable(rows, cnt) =>
        (0 until cnt).map(_ => "[]").mkString(",")
    }
    merged.mkString("(", ",", ")")
  }

}
