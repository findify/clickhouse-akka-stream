package io.findify.clickhouse.example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.findify.clickhouse.ClickhouseClient
import io.findify.clickhouse.format.decoder.generic._
import io.findify.clickhouse.format.decoder.generic.auto._

class ReadExample {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem.create("clickhouse")
    implicit val mat = ActorMaterializer()
    import system.dispatcher

    case class Simple(key: String, value: Int)
    implicit val decoder = deriveDecoder[Simple]


    val client = new ClickhouseClient(host = "localhost", port = 8123)
    for {
      ddl <- client.execute("create table simple(key String, value: Int32) ENGINE = Memory")
      insert <- client.execute("""insert into simple values ("a",1),("b",2)""")
      rows <- client.query("select * from simple order by value asc").map(_.data.map(_.as[Simple]))
    } yield {
      println(rows)
      // will print: List(Simple("a",1), Simple("b",2))
    }
  }
}
