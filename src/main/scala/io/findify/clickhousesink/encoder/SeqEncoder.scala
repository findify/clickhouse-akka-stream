package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, NestedTable, Row}

class SeqEncoder[T <: Product](implicit encoder: Encoder[T]) extends Encoder[Seq[T]] {
  override def encode(value: Seq[T]): Seq[Field] = Seq(NestedTable(value.map(r => Row(encoder.encode(r)))))

  override def asString(value: Seq[T]): String = ???
}
