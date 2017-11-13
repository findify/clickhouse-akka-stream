package io.findify.clickhouse.encoder
import io.findify.clickhouse.CustomMapper
import io.findify.clickhouse.field.{Field, SimpleField}

class OptionEncoder[T](implicit encoder: ScalarEncoder[T]) extends Encoder[Option[T]] {
  override def ddl(name: String, mapper: CustomMapper, separator: String): String = s"$name Nullable(${encoder.fieldType(name, mapper)})"
  override def encode(value: Option[T]): Seq[Field] = value match {
    case Some(internal) => encoder.encode(internal)
    case None => Seq(SimpleField("null"))
  }
}
