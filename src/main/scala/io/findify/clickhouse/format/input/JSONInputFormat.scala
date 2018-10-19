package io.findify.clickhouse.format.input
import akka.util.ByteString
import io.circe.{Decoder, DecodingFailure}
import io.findify.clickhouse.format
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.semiauto._
import io.findify.clickhouse.format.{ClickhouseError, Field}
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.input.InputFormat.{Response, Statistics, TableMeta}
import io.findify.clickhouse.format.input.JSONInputFormat._
import cats.syntax.either._

class JSONInputFormat extends InputFormat {
  implicit val fieldTypeDecoder = deriveDecoder[FieldType]
  implicit val tableMetaDecoder: Decoder[TableMeta] = Decoder.instance(c => {
    for {
      fields <- c.as[List[FieldType]]
    } yield {
      TableMeta(fields.map(f => f.name -> f.`type`).toMap)
    }
  })

  override def name: String = "JSON"
  implicit def rowDecoder(implicit meta: TableMeta): Decoder[Row] = Decoder.instance(cursor => {
    val cells = (for {
      fields <- cursor.keys.toList
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
  implicit val statsDecoder: Decoder[Statistics] = deriveDecoder[Statistics]
  implicit val responseDecoder: Decoder[Response] = Decoder.instance(cursor => {
    cursor.downField("meta").as[TableMeta] match {
      case Right(meta) =>
        implicit val imeta = meta
        for {
          data <- cursor.downField("data").as[List[Row]]
          rows <- cursor.downField("rows").as[Int]
          rowsBeforeLimit <- cursor.downField("rows_before_limit_at_least").as[Option[Int]]
          stats <- cursor.downField("statistics").as[Option[Statistics]]
        } yield {
          Response(meta, data, rows, stats, rowsBeforeLimit)
        }
      case Left(err) => Left(err)
    }
  })
  override def read(data: ByteString): Either[format.ClickhouseError, Response] = {
    decode[Response](data.utf8String) match {
      case Left(err) => Left(JsonDecodingError(err))
      case Right(response) => Right(response)
    }
  }
}

object JSONInputFormat {
  case class FieldType(name: String, `type`: String)

  case class JsonDecodingError(nested: io.circe.Error) extends ClickhouseError(nested.getMessage)
}
