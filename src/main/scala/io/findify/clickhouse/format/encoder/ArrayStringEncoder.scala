package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CArray, CString, ScalarField}

class ArrayStringEncoder(implicit val enc: ScalarEncoder[String, CString]) extends ScalarEncoder[Seq[String], CArray[CString]] {
  override def encode(value: Seq[String]): CArray[CString] = CArray(value.map(enc.encode))
}
