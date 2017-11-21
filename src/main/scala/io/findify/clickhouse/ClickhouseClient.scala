package io.findify.clickhouse

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Source
import akka.stream.{Materializer, Outlet, SourceShape}
import akka.stream.stage.GraphStage
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import io.findify.clickhouse.ClickhouseClient.QueryError
import io.findify.clickhouse.format.ClickhouseError
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.input.{InputFormat, JSONInputFormat}
import io.findify.clickhouse.format.input.InputFormat.Response
import io.findify.clickhouse.format.output.OutputFormat

import scala.concurrent.Future

class ClickhouseClient(host: String, port: Int, format: InputFormat = new JSONInputFormat())(implicit val sys: ActorSystem, mat: Materializer) extends LazyLogging {
  private val http = Http()

  def query(str: String): Future[Response] = {

    import sys.dispatcher
    logger.debug(s"request: $str")
    http.singleRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(s"http://$host:$port/"),
        entity = s"$str FORMAT ${format.name}"
      )
    ).flatMap {
      case HttpResponse(StatusCodes.OK, _, HttpEntity.Strict(_, bytes), _) =>
        logger.debug(s"response: ${bytes.utf8String}")
        asFuture(format.read(bytes))
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.runFold(ByteString(""))(_ ++ _).flatMap(bytes => {
          logger.debug(s"response: ${bytes.utf8String}")
          asFuture(format.read(bytes))
        })
      case HttpResponse(code, _, entity, _) => entity.dataBytes.runFold(ByteString(""))(_ ++ _).flatMap(bytes => {
        logger.debug(s"response: ${bytes.utf8String}")
        Future.failed(QueryError(bytes.utf8String))
      })
    }
  }

  def execute(str: String): Future[Done] = {
    import sys.dispatcher
    logger.debug(s"request: $str")
    http.singleRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(s"http://$host:$port/"),
        entity = str
      )
    ).flatMap {
      case HttpResponse(StatusCodes.OK, _, HttpEntity.Strict(_, bytes), _) =>
        logger.debug(s"response: ${bytes.utf8String}")
        Future.successful(Done)
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(bytes => {
        logger.debug(s"response: ${bytes.utf8String}")
        Done
      })
      case HttpResponse(code, _, entity, _) => entity.dataBytes.runFold(ByteString(""))(_ ++ _).flatMap(err => {
        logger.debug(s"response: ${err.utf8String}")
        Future.failed(QueryError(err.utf8String))
      })
    }

  }

  private def asFuture(response: Either[ClickhouseError, Response]): Future[Response] = response match {
    case Right(r) => Future.successful(r)
    case Left(err) => Future.failed(err)
  }
}

object ClickhouseClient {
  case class QueryError(message: String) extends ClickhouseError(message)
}