package io.findify.clickhouse.encoder

import io.findify.clickhouse.CustomMapper
import io.findify.clickhouse.field.{Field, SimpleField}

trait ScalarEncoder[T] extends Encoder[T] {
  protected def defaultType: String
  def encodeRaw(value: T): String

  def fieldType(name: String, mapper: CustomMapper): String = mapper.fieldType(name, defaultType)
  override def ddl(name: String, mapper: CustomMapper, separator: String): String = s"${mapper.fieldName(name)} ${mapper.fieldType(name, fieldType(name, mapper))}"
  override def encode(value: T): Seq[Field] = Seq(SimpleField(encodeRaw(value)))
}
