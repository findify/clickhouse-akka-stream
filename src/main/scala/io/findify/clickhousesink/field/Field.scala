package io.findify.clickhousesink.field

sealed trait Field {
  def name: String
  def tpe: String
}

case class Row(values: Seq[Field])
case class SimpleField(name: String, tpe: String, value: String) extends Field
case class ScalarField(tpe: String, value: String)
case class NestedTable(name: String, values: Seq[Row]) extends Field {
  def tpe = values.headOption match {
    case None => ???
    case Some(row) => row.values.map {
      case SimpleField(fname, ftpe, _) => s"$fname $ftpe"
      case _ => ???
    }.mkString("Nested(", ",", ")")
  }
}
case class ArrayField(name: String, values: Seq[ScalarField]) extends Field {
  def tpe = values.headOption match {
    case Some(ScalarField(vtpe, _)) => s"Array<$vtpe>"
    case None => ???
  }
}
