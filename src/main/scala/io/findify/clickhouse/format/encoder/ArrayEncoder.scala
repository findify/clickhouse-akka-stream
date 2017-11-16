package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CArray, ScalarField}

class ArrayEncoder[T <: AnyVal](implicit enc: ScalarEncoder[T]) extends ScalarEncoder[Seq[T]] {
  override def encode(value: Seq[T]): ScalarField = CArray(value.map(enc.encode))
}
