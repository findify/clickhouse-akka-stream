package io.findify.clickhouse.format.input

import akka.util.ByteString
import cats.syntax.either._
import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.parser._
import io.findify.clickhouse.format
import io.findify.clickhouse.format.ClickhouseError
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.input.InputFormat.{Response, Statistics, TableMeta}

import scala.collection.immutable.ListMap

trait GenericJSONInputFormat extends InputFormat {

  import GenericJSONInputFormat._

  implicit def rowDecoder(implicit meta: TableMeta): Decoder[Row]

  implicit val fieldTypeDecoder = deriveDecoder[FieldType]
  implicit val tableMetaDecoder: Decoder[TableMeta] = Decoder.instance(c => {
    for {
      fields <- c.as[List[FieldType]]
    } yield {
      TableMeta(ListMap(fields.map(f => f.name -> f.`type`): _*))
    }
  })

  implicit val statsDecoder: Decoder[Statistics] = deriveDecoder[Statistics]
  implicit val responseDecoder: Decoder[Response] = Decoder.instance(cursor => {
    cursor.downField("meta").as[TableMeta] match {
      case Right(meta) =>
        implicit val imeta = meta
        for {
          data            <- cursor.downField("data").as[List[Row]]
          rows            <- cursor.downField("rows").as[Int]
          rowsBeforeLimit <- cursor.downField("rows_before_limit_at_least").as[Option[Int]]
          stats           <- cursor.downField("statistics").as[Option[Statistics]]
        } yield {
          Response(meta, data, rows, stats, rowsBeforeLimit)
        }
      case Left(err) => Left(err)
    }
  })
  override def read(data: ByteString): Either[format.ClickhouseError, Response] = {
    decode[Response](data.utf8String) match {
      case Left(err)       => Left(JsonDecodingError(err))
      case Right(response) => Right(response)
    }
  }

}

object GenericJSONInputFormat {
  case class FieldType(name: String, `type`: String)

  case class JsonDecodingError(nested: io.circe.Error) extends ClickhouseError(nested.getMessage)
}
