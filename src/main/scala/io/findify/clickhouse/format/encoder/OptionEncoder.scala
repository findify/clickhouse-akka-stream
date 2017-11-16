package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{Nullable, ScalarField}

class OptionEncoder[T, F <: ScalarField](implicit enc: ScalarEncoder[T, F]) extends ScalarEncoder[Option[T], Nullable[F]] {
  override def encode(value: Option[T]): Nullable[F] = value match {
    case None => Nullable(None)
    case Some(in) => Nullable(Some(enc.encode(in)))
  }
}
