package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CString, Row}

trait Decoder[T, +F <: Field] extends Serializable {
  def decodeValue: PartialFunction[Field, T]
  def decode(name: String, row: Row): T = row.fields.find(_._1 == name) match {
    case Some(in) =>
      decodeValue.lift.apply(in._2) match {
        case Some(value) => value
        case None        => throw new IllegalArgumentException(s"$name=$in cannot be decoded as ${this.getClass.getTypeName}")
      }
    case None => throw new IllegalArgumentException(s"field $name cannot be found in data")
  }
}
