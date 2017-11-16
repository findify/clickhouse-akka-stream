package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Nullable, ScalarField}

class OptionEncoder[T](implicit enc: ScalarEncoder[T]) extends ScalarEncoder[Option[T]] {
  override def encode(value: Option[T]): ScalarField = value match {
    case None => Nullable(None)
    case Some(in) => Nullable(Some(enc.encode(in)))
  }
}
