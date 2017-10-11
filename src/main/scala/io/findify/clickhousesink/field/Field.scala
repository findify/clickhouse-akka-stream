package io.findify.clickhousesink.field

sealed trait Field {
  def name: String
}

case class Row(values: Seq[Field])
case class SimpleField(name: String, value: String) extends Field
//case class NullableField(name: String, value: String) extends Field
case class ScalarField(value: String)
case class NestedTable(name: String, values: Seq[Row]) extends Field
case class ArrayField(name: String, values: Seq[ScalarField]) extends Field
