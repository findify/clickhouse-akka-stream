package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field, NestedTable, Row}

class SeqEncoder[T <: Product](implicit encoder: Encoder[T]) extends Encoder[Seq[T]] {
  override def ddl(name: String, mapper: CustomMapper): String = s"$name Nested(${encoder.ddl(name, mapper)})"
  override def encode(name: String, value: Seq[T], mapper: CustomMapper): Seq[Field] = Seq(NestedTable(name, value.map(r => Row(encoder.encode("", r, mapper)))))
}
