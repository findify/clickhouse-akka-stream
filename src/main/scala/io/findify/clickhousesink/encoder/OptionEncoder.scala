package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, SimpleField}

class OptionEncoder[T](implicit encoder: Encoder[T]) extends Encoder[Option[T]] {
  override def ddl(name: String): String = ???
  override def encode(name: String, value: Option[T]): Seq[Field] = ???
}
