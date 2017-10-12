[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.findify/clickhouse-akka-stream_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.findify/clickhouse-akka-stream_2.12)

# Clickhouse sink for akka-stream

This is a prototype of akka-stream compatible Sink for Clickhouse database.
Main features:

* Can transparently write any case class (if key names and types match).
* Able to generate DDL for arbitrary case class.
* has batching, back-pressure and stuff.
* supports Array, Nested data types
* nullable fields
* custom types for scalar decoders (i.e. scala.Int can be mapped to CH UInt8)

TODO:
* moar testing

## Usage

Build artifacts are available on maven-central for Scala 2.12. For SBT, add this snippet to `build.sbt`:
```scala
libraryDependencies += "io.findify" %% "clickhouse-akka-stream" % "0.1.2"
```

## Example

DDL for a simple case class:

```scala
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._
  
  case class Simple(key: String, value: Int)
  
  val encoder = deriveEncoder[Simple]
  encoder.schema("simple", "ENGINE = Memory") 
  // will emit "CREATE TABLE simple (key String,value Int32) ENGINE = Memory"
```


DDL for more complex case class:

```scala
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._

  case class Nested(foo: String, bar: Int)
  case class Root(key: String, arr: Seq[Int], values: Seq[Nested])
  val encoder = deriveEncoder[Root]
  encoder.schema("root", "ENGINE = Memory")
  // will emit "CREATE TABLE root (key String,arr Array<Int32>,values Nested(foo String,bar Int32)) ENGINE = Memory"

```
Writing rows to database:

```scala
  import akka.actor.ActorSystem
  import akka.stream.ActorMaterializer
  import akka.stream.scaladsl.{Keep, Sink, Source}
  import io.findify.clickhousesink.ClickhouseSink
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._
  
  case class Simple(key: String, value: Int)
  implicit val encoder = deriveEncoder[Simple]

  val source = Source(List(Simple("a", 1), Simple("b", 2), Simple("c", 3)))
  val sink = Sink.fromGraph(new ClickhouseSink[Simple](
    host = "localhost",
    port = 8123,
    table = "simple"
  ))
  val result = source.runWith(sink)
  // will perform "INSERT INTO simple VALUES ('a', 1),('b',2),('c',3);"
```

Implementing your own scalar type support:
```scala
  import io.findify.clickhousesink.encoder._
  import io.findify.clickhousesink.encoder.generic._
  import io.findify.clickhousesink.encoder.generic.auto._
  
  case class Color(r: Byte, g: Byte, b: Byte)
  case class Simple(key: String, custom: Color)
  // Look, mom, it's a typeclass!
  implicit val colorEncoder = new ScalarEncoder[Color] {
    override def defaultType: String = "String"
    override def encodeRaw(value: Color): String = s"'r=${value.r},g=${value.g},b=${value.b}'"
  }
  val encoder = deriveEncoder[Simple]
  encoder.schema("simple", "ENGINE = Memory")
  // will emit "CREATE TABLE simple (key String,value String) ENGINE = Memory"
  val instance = Simple("foo", Color(1,2,3)) 
  // if written via ClickhouseSink, will emit "INSERT INTO simple VALUES ('foo', 'r=1,g=2,b=3')"

```

## Licence

The MIT License (MIT)

Copyright (c) 2017 Findify AB

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.