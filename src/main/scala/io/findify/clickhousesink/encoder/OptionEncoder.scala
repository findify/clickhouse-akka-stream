package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, SimpleField}

class OptionEncoder[T](implicit encoder: ScalarEncoder[T]) extends Encoder[Option[T]] {
  override def ddl(name: String): String = s"$name Nullable(${encoder.ddlScalar})"
  override def encode(name: String, value: Option[T]): Seq[Field] = value match {
    case Some(internal) => encoder.encode(name, internal)
    case None => Seq(SimpleField(name, "null"))
  }
}
