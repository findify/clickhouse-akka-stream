[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.findify/clickhouse-akka-stream_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.findify/clickhouse-akka-stream_2.12)

# Clickhouse sink for akka-stream

This is a prototype of akka-stream compatible Sink for Clickhouse database.
Main features:

* Can transparently write any case class (if key names and types match).
* has batching, back-pressure and stuff.
* supports Array, Nested data types
* nullable fields

TODO:
* moar testing

## Usage

Build artifacts are available on maven-central for Scala 2.12. For SBT, add this snippet to `build.sbt`:
```scala
libraryDependencies += "io.findify" %% "clickhouse-akka-stream" % "0.3.0"
```

## Example

Writing rows to database:

```scala
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import io.findify.clickhouse.ClickhouseSink
import io.findify.clickhouse.format.encoder.generic._
import io.findify.clickhouse.format.encoder.generic.auto._


class WriteExample {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem.create("clickhouse")
    implicit val mat = ActorMaterializer()

    case class Simple(key: String, value: Int)
    implicit val encoder = deriveEncoder[Simple]

    val source = Source(List(Simple("a", 1), Simple("b", 2), Simple("c", 3)))
      .map(_.asRow)
    val sink = Sink.fromGraph(new ClickhouseSink(
      host = "localhost",
      port = 8123,
      table = "simple"
    ))
    val result = source.runWith(sink)
    // will perform:
    // INSERT INTO simple FORMAT JSONEachRow
    // {"key": "a", "value": 1}
    // {"key": "b", "value": 2}
    // {"key": "c", "value": 3}
  }
}
```

Reading rows from database:
```scala
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
      ddl <- client.execute("create table simple(key String, value Int32) ENGINE = Memory")
      insert <- client.execute("""insert into simple values ("a",1),("b",2)""")
      rows <- client.query("select * from simple order by value asc").map(_.data.map(_.as[Simple]))
    } yield {
      println(rows)
      // will print: List(Simple("a",1), Simple("b",2))
    }
  }
}
```

## Licence

The MIT License (MIT)

Copyright (c) 2017 Findify AB

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.