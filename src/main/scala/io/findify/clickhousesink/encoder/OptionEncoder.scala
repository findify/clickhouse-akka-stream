package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field, SimpleField}

class OptionEncoder[T](implicit encoder: ScalarEncoder[T]) extends Encoder[Option[T]] {
  override def ddl(name: String, mapper: CustomMapper): String = s"$name Nullable(${encoder.fieldType(name, mapper)})"
  override def encode(name: String, value: Option[T], mapper: CustomMapper): Seq[Field] = value match {
    case Some(internal) => encoder.encode(name, internal, mapper)
    case None => Seq(SimpleField(name, "null"))
  }
}
