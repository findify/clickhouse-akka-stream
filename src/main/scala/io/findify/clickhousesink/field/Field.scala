package io.findify.clickhousesink.field

sealed trait Field {
  def name: String
  def tpe: String
}

case class Row(values: Seq[Field])
case class SimpleField(name: String, tpe: String, value: String) extends Field
case class NullableField(name: String, tpeInternal: String, value: String) extends Field {
  override def tpe: String = s"Nullable<$tpeInternal>"
}
case class ScalarField(tpe: String, value: String)
case class NestedTable(name: String, values: Seq[Row]) extends Field {
  def tpe = values.headOption match {
    case None => ???
    case Some(row) => row.values.map {
      case SimpleField(fname, ftpe, _) => s"$fname $ftpe"
      case n @ NullableField(fname, _, _) => s"$fname ${n.tpe}"
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
