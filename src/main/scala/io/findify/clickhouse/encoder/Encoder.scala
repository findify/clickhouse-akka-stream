package io.findify.clickhouse.encoder

import io.findify.clickhouse.CustomMapper
import io.findify.clickhouse.field.{Field}

trait Encoder[T] {
  def fieldCount: Int = 1
  def encode(value: T): Seq[Field]
  def ddl(name: String, mapper: CustomMapper = CustomMapper.Noop, separator: String = ","): String

  def schema(table:String, options: String, mapper: CustomMapper = CustomMapper.Noop, separator: String = ","): String = s"CREATE TABLE $table (${ddl("root", mapper, separator)}) $options"
}
