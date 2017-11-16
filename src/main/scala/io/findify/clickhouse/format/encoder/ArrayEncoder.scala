package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CArray, ScalarField}

class ArrayEncoder[T <: AnyVal, F <: ScalarField](implicit enc: ScalarEncoder[T,F]) extends ScalarEncoder[Seq[T], CArray[F]] {
  override def encode(value: Seq[T]): CArray[F] = CArray(value.map(enc.encode))
}
