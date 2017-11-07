package io.findify.clickhousesink.field

sealed trait Field

case class Row(values: Seq[Field])
case class SimpleField(value: String) extends Field
case class NestedTable(values: Seq[Row], fieldCount: Int = 1) extends Field
case class ArrayField(values: Seq[String]) extends Field
