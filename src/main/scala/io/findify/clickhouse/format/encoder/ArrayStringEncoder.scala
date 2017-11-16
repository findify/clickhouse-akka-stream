package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CArray, ScalarField}

class ArrayStringEncoder(implicit val enc: ScalarEncoder[String]) extends ScalarEncoder[Seq[String]] {
  override def encode(value: Seq[String]): ScalarField = CArray(value.map(enc.encode))
}
