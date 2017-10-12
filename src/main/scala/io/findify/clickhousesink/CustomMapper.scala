package io.findify.clickhousesink

trait CustomMapper {
  def fieldName(name: String): String
  def fieldType(name: String, currentType: String): String
}

object CustomMapper {
  object Noop extends CustomMapper {
    override def fieldName(name: String): String = name
    override def fieldType(name: String, currentType: String): String = currentType
  }
}
