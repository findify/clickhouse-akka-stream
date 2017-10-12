package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field, ScalarField, SimpleField}

trait ScalarEncoder[T] extends Encoder[T] {
  protected def defaultType: String
  protected def encodeRaw(value: T): String

  def fieldType(name: String, mapper: CustomMapper): String = mapper.fieldType(name, defaultType)
  override def ddl(name: String, mapper: CustomMapper): String = s"${mapper.fieldName(name)} ${mapper.fieldType(name, fieldType(name, mapper))}"
  override def encode(name: String, value: T, mapper: CustomMapper): Seq[Field] = Seq(SimpleField(mapper.fieldName(name), encodeRaw(value)))
  def encodeScalar(value: T): Seq[ScalarField] = Seq(ScalarField(encodeRaw(value)))
}
