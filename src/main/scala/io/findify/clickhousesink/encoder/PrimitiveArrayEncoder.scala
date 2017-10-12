package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{ArrayField, Field, NestedTable}

class PrimitiveArrayEncoder[T <: AnyVal](implicit val encoder: ScalarEncoder[T]) extends Encoder[Seq[T]]{
  override def ddl(name: String, mapper: CustomMapper): String = s"$name Array(${encoder.fieldType(name, mapper)})"
  override def encode(value: Seq[T]): Seq[Field] = Seq(ArrayField(value.map(encoder.encodeRaw)))
}
