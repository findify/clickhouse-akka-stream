package io.findify.clickhouse.format.input
import io.circe.{Decoder, DecodingFailure}
import io.findify.clickhouse.format
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.semiauto._
import io.findify.clickhouse.format.{ClickhouseError, Field}
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.input.JSONInputFormat._

class JSONInputFormat extends InputFormat {
  implicit val fieldTypeDecoder = deriveDecoder[FieldType]
  implicit val tableMetaDecoder: Decoder[TableMeta] = Decoder.instance(c => {
    for {
      fields <- c.as[List[FieldType]]
    } yield {
      TableMeta(fields.map(f => f.name -> f.`type`).toMap)
    }
  })
  implicit def rowDecoder(implicit meta: TableMeta): Decoder[Row] = Decoder.instance(cursor => {
    val cells = (for {
      fields <- cursor.fields.toList
      field <- fields
      fieldType <- meta.fields.get(field)
      fieldValue <- cursor.downField(field).focus
    } yield {
      field -> Field(fieldType, fieldValue)
    }).toMap
    if (cells.size == meta.fields.size) {
      Right(Row(cells))
    } else {
      Left(DecodingFailure("cannot decode all the fields", cursor.history))
    }
  })
  implicit val responseDecoder: Decoder[Response] = Decoder.instance(cursor => {
    cursor.downField("meta").as[TableMeta] match {
      case Right(meta) =>
        implicit val imeta = meta
        cursor.downField("data").as[List[Row]] match {
          case Right(rows) => Right(Response(meta, rows, 0))
          case Left(err) => Left(err)
        }
      case Left(err) => Left(err)
    }
  })
  override def read(data: Array[Byte]): Either[format.ClickhouseError, Seq[Row]] = {
    decode[Response](new String(data)) match {
      case Left(err) => Left(JsonDecodingError(err))
      case Right(response) => Right(response.data)
    }
  }
}

object JSONInputFormat {
  case class FieldType(name: String, `type`: String)
  case class TableMeta(fields: Map[String,String])
  case class Response(meta: TableMeta,  data: List[Row], rows: Int)

  case class JsonDecodingError(nested: io.circe.Error) extends ClickhouseError
}
