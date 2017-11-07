package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field, NestedTable, Row}

class SeqEncoder[T <: Product](implicit encoder: Encoder[T]) extends Encoder[Seq[T]] {
  override def ddl(name: String, mapper: CustomMapper, separator: String): String = s"$name Nested(${encoder.ddl(name, mapper, separator)})"
  override def encode(value: Seq[T]): Seq[Field] = Seq(NestedTable(value.map(r => Row(encoder.encode(r))), encoder.fieldCount))
}
