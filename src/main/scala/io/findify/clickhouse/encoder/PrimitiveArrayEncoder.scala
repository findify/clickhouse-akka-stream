package io.findify.clickhouse.encoder
import io.findify.clickhouse.CustomMapper
import io.findify.clickhouse.field.{ArrayField, Field, NestedTable}

class PrimitiveArrayEncoder[T <: AnyVal](implicit val encoder: ScalarEncoder[T]) extends Encoder[Seq[T]]{
  override def ddl(name: String, mapper: CustomMapper, separator: String): String = s"$name Array(${encoder.fieldType(name, mapper)})"
  override def encode(value: Seq[T]): Seq[Field] = Seq(ArrayField(value.map(encoder.encodeRaw)))
}
