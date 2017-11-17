package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.{Field, Scalar}
import io.findify.clickhouse.format.Field.{CArray, ScalarField}

class ArrayEncoder[T, F <: ScalarField](implicit enc: ScalarEncoder[T,F], s: Scalar[T]) extends ScalarEncoder[Seq[T], CArray[F]] {
  override def encode(value: Seq[T]): CArray[F] = CArray(value.map(enc.encode))
}
