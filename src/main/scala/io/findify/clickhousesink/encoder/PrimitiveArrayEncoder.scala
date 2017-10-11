package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{ArrayField, Field, NestedTable}

class PrimitiveArrayEncoder[T <: AnyVal](implicit val encoder: ScalarEncoder[T]) extends Encoder[Seq[T]]{
  override def ddl(name: String): String = s"$name Array<${encoder.ddlScalar}>"
  override def encode(name: String, value: Seq[T]): Seq[Field] = Seq(ArrayField(name, value.flatMap(encoder.encodeScalar)))
}
