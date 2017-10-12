package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.{Field}

trait Encoder[T] {
  def encode(value: T): Seq[Field]
  def ddl(name: String, mapper: CustomMapper = CustomMapper.Noop): String

  def schema(table:String, options: String, mapper: CustomMapper = CustomMapper.Noop): String = s"CREATE TABLE $table (${ddl("root", mapper)}) $options"
}
