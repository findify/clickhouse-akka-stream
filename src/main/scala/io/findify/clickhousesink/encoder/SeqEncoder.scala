package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field, NestedTable, Row}

class SeqEncoder[T <: Product](implicit encoder: Encoder[T]) extends Encoder[Seq[T]] {
  override def ddl(name: String, mapper: CustomMapper): String = s"$name Nested(${encoder.ddl(name, mapper)})"
  override def encode(value: Seq[T]): Seq[Field] = Seq(NestedTable(value.map(r => Row(encoder.encode(r)))))
}
