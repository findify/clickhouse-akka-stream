package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.{Field, Scalar}
import io.findify.clickhouse.format.Field.{Nullable, ScalarField}

class OptionEncoder[T, F <: ScalarField](implicit dec: Decoder[T, F], s: Scalar[T]) extends Decoder[Option[T], Nullable[F]] {
  override def decodeValue: PartialFunction[Field, Option[T]] = {
    case Nullable(Some(in)) => dec.decodeValue.lift(in)
    case Nullable(None) => None
  }
}
