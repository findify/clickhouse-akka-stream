package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field.{Field, ScalarField}

trait Encoder[T] {
  def encode(name: String, value: T): Seq[Field]
//  def ddl(name: String) = s"$name $tpe"
}
