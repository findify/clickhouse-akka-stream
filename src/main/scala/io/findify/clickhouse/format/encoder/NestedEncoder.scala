package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CNested, Row, ScalarField, ScalarRow}

class NestedEncoder[T <: Product, F <: Field](implicit enc: Encoder[T,F]) extends Encoder[Seq[T], CNested] {
  override def encode(name: String, value: Seq[T]): Map[String, CNested] = Map(name -> CNested(value.map(row => ScalarRow(enc.encode(name, row)))))
}
