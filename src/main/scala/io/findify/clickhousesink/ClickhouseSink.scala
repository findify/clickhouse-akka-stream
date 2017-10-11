package io.findify.clickhousesink

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Attributes, Inlet, SinkShape}
import akka.stream.stage._
import akka.util.ByteString
import io.circe.{Encoder, Json, JsonObject}
import io.findify.clickhousesink.ClickhouseSink.Options

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import io.circe.syntax._
import io.findify.clickhousesink.field.{ArrayField, Field, NestedTable, SimpleField}

import scala.concurrent.{Future, Promise}
import scala.util.parsing.json.JSONArray

class ClickhouseSink[T](host: String, port: Int, table: String, options: ClickhouseSink.Options = Options())(implicit val system: ActorSystem, encoder: Encoder[T]) extends GraphStageWithMaterializedValue[SinkShape[T], Future[Done]] {
  val in: Inlet[T] = Inlet("input")
  override val shape: SinkShape[T] = SinkShape(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Done]) = {
    val promise = Promise[Done]
    val logic = new GraphStageLogic(shape) with StageLogging {
      val http = Http(system)
      implicit val mat = ActorMaterializer()
      import system.dispatcher
      val buffer = ArrayBuffer[String]()

      override def preStart(): Unit = pull(in)

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val item = grab(in)
          val json = item.asJson.noSpaces
          buffer.append(json)
          pull(in)
        }

        override def onUpstreamFinish(): Unit = {
          log.info("done")
          val data = buffer.mkString(s"INSERT INTO $table FORMAT JSONEachRow\n", "\n", "")
          log.info(data)
          http.singleRequest(HttpRequest(
            method = HttpMethods.POST,
            uri = Uri(s"http://$host:$port/"),
            entity = data)
          ).map {
            case HttpResponse(StatusCodes.OK, _, entity, _) =>
              entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String).map(line => {
                log.info(line)
                promise.success(Done)
                completeStage()
              })
            case HttpResponse(code, _, entity, _) =>
              entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String).map(line => {
                log.error(line)
                val ex = new IllegalArgumentException("non-200 response from clickhouse")
                promise.failure(ex)
                failStage(ex)
              })
          }
        }
      })
    }
    (logic, promise.future)
  }
}
object ClickhouseSink {
  case class Options(batchSize: Int = 128, batchFlushTimeout: Duration = 10.seconds)

  def flatten(item: Seq[Field]): String = {
    val merged = item.map {
      case SimpleField(value) => value
      case ArrayField(values) => values.mkString("[", ",", "]")
      case NestedTable(rows) =>
        rows.map(_.values).transpose.map(col => col.map {
          case SimpleField(value) => value
          case _ => ???
        }.mkString("[", ",", "]")).mkString(",")
    }
    merged.mkString("(", ",", ")")
  }
}
