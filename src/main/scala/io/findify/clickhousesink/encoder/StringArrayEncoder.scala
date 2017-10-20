package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{ArrayField, Field}


class StringArrayEncoder(implicit val encoder: ScalarEncoder[String]) extends Encoder[Seq[String]]{
  override def ddl(name: String, mapper: CustomMapper, separator: String): String = s"$name Array(${encoder.fieldType(name, mapper)})"
  override def encode(value: Seq[String]): Seq[Field] = Seq(ArrayField(value.map(encoder.encodeRaw)))
}
