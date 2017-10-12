package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field, SimpleField}

trait ScalarEncoder[T] extends Encoder[T] {
  protected def defaultType: String
  def encodeRaw(value: T): String

  def fieldType(name: String, mapper: CustomMapper): String = mapper.fieldType(name, defaultType)
  override def ddl(name: String, mapper: CustomMapper): String = s"${mapper.fieldName(name)} ${mapper.fieldType(name, fieldType(name, mapper))}"
  override def encode(value: T): Seq[Field] = Seq(SimpleField(encodeRaw(value)))
}
