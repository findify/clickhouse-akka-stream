package io.findify.clickhouse.format.input

import io.circe.{Decoder, DecodingFailure}
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.input.InputFormat.TableMeta

class JSONInputFormat extends GenericJSONInputFormat {

  override def name: String = "JSON"

  implicit def rowDecoder(implicit meta: TableMeta): Decoder[Row] = Decoder.instance(cursor => {
    val cells = (for {
      fields     <- cursor.keys.toList
      field      <- fields
      fieldType  <- meta.getFieldType(field)
      fieldValue <- cursor.downField(field).focus
    } yield {
      field -> Field(fieldType, fieldValue)
    })
    if (cells.size == meta.fields.size) {
      Right(Row(cells))
    } else {
      Left(DecodingFailure("cannot decode all the fields", cursor.history))
    }
  })

}
