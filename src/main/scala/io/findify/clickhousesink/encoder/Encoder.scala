package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field.Field

trait Encoder[T] {
  def encode(value: T): Seq[Field]
  def asString(value: T): String
}
