package io.findify.clickhouse.format.input

import io.circe.{Decoder, DecodingFailure}
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.input.InputFormat.TableMeta

class JSONCompactInputFormat extends GenericJSONInputFormat {

  override def name: String = "JSONCompact"

  implicit def rowDecoder(implicit meta: TableMeta): Decoder[Row] = Decoder.instance(cursor => {
    val br = 1
    val cells: Map[String, Field] =
      cursor.values
        .getOrElse(Seq.empty)
        .zip(meta.fields)
        .map({ case (fieldValue, (fieldName, fieldType)) =>
          fieldName -> Field(fieldType, fieldValue)
        })
        .toMap

    if (cells.size == meta.fields.size) {
      Right(Row(cells))
    } else {
      Left(DecodingFailure("cannot decode all the fields", cursor.history))
    }
  })

}
