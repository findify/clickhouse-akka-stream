package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, SimpleField}

class IntEncoder extends Encoder[Int] {
  override def encode(value: Int): Seq[Field] = Seq(SimpleField(value.toString))
  override def asString(value: Int): String = value.toString
}
