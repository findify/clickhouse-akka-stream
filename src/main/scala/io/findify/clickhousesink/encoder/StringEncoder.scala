package io.findify.clickhousesink.encoder


class StringEncoder extends ScalarEncoder[String] {
  override def defaultType: String = "String"
  override def encodeRaw(value: String): String = "'" + quote(value) + "'"

  def quote(input: String): String = {
    val result = new StringBuilder()
    var pos = 0
    while (pos < input.length) {
      input.charAt(pos) match {
        case '\\' => result.append("\\\\")
        case '\'' => result.append("\\\'")
        case other => result.append(other)
      }
      pos += 1
    }
    result.toString()
  }
}
