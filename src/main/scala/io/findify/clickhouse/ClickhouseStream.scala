package io.findify.clickhouse

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri.Query
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import io.findify.clickhouse.format.output.OutputFormat

import scala.concurrent.Future

trait ClickhouseStream extends LazyLogging {
  def table: String
  def format: OutputFormat
  def host: String
  def port: Int
  def flush(stream: Source[ByteString, _])(implicit system: ActorSystem, mat: Materializer): Future[Done] = {
    val http = Http()
    val query = s"INSERT INTO $table FORMAT ${format.name}"
    logger.debug(s"query: $query")
    import system.dispatcher
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
