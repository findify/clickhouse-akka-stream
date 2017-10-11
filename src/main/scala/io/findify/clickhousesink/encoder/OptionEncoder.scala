package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, NullableField, SimpleField}

class OptionEncoder[T](implicit encoder: Encoder[T]) extends Encoder[Option[T]] {
  override def encode(name: String, value: Option[T]): Seq[Field] = ???
}
