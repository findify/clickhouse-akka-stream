package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{ArrayField, Field, NestedTable}

class PrimitiveArrayEncoder[T <: AnyVal](implicit val encoder: Encoder[T]) extends Encoder[Seq[T]]{
  override def encode(value: Seq[T]): Seq[Field] = Seq(ArrayField(value.map(encoder.asString)))
  override def asString(value: Seq[T]): String = value.map(encoder.asString).mkString("[", ",", "]")
}
