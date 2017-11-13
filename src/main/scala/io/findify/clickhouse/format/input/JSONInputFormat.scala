package io.findify.clickhouse.format.input
import io.findify.clickhouse.format
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.semiauto._
import io.findify.clickhouse.format.{Cell, ClickhouseError, Row}
import io.findify.clickhouse.format.input.JSONInputFormat.{ColumnType, JsonDecodingError, Response}

class JSONInputFormat extends InputFormat {
  implicit val columnTypeDecoder = deriveDecoder[ColumnType]
  implicit val responseDecoder = deriveDecoder[Response]
  override def read(data: Array[Byte]): Either[format.ClickhouseError, Seq[Row]] = {
    decode[Response](new String(data)) match {
      case Left(err) => Left(JsonDecodingError(err))
      case Right(response) =>
        val columnTypes = response.meta.map(kv => kv.name -> kv.`type`).toMap
        val rows = for {
          row <- response.data
        } yield {
          val cells = for {
            (key, value) <- row
            valueType <- columnTypes.get(key)
          } yield {
            Cell(key, valueType, value)
          }
          Row(cells.toList)
        }
        Right(rows)
    }
  }
}

object JSONInputFormat {
  case class ColumnType(name: String, `type`: String)
  case class Response(meta: List[ColumnType],  data: List[Map[String,String]], rows: Int)

  case class JsonDecodingError(nested: io.circe.Error) extends ClickhouseError
}
