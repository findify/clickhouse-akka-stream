package io.findify.clickhouse.format.encoder
import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CString, ScalarField}

class StringEncoder extends ScalarEncoder[String, CString] {
  override def encode(value: String): CString = CString(value)
}
