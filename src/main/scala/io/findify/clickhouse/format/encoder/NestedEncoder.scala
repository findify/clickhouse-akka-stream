package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CNested, Row, ScalarField, ScalarRow}

class NestedEncoder[T <: Product](implicit enc: Encoder[T]) extends Encoder[Seq[T]] {
  override def encodeS(name: String, value: Seq[T]): Map[String, ScalarField] = ???
  override def encode(name: String, value: Seq[T]): Map[String, Field] = Map(name -> CNested(value.map(row => ScalarRow(enc.encodeS(name, row)))))
}
