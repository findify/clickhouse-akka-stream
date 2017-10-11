package io.findify.clickhousesink

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import akka.util.ByteString

import scala.concurrent.Future

class Client(host: String, port: Int)(implicit val system: ActorSystem) {
  implicit val mat = ActorMaterializer()
  val http = Http()

  def query(q: String): Future[String] = {
    import system.dispatcher
    http.singleRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(s"http://$host:$port/"),
        entity = q
      )
    ).flatMap {
      case HttpResponse(StatusCodes.OK, _, HttpEntity.Strict(_, bytes), _) => Future.successful(bytes.utf8String)
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)
      case HttpResponse(code, _, entity, _) => entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)
    }
  }
}
