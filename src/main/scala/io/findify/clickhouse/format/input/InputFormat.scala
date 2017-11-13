package io.findify.clickhouse.format.input

import io.findify.clickhouse.format.{ClickhouseError, Row}

trait InputFormat {
  def read(data: Array[Byte]): Either[ClickhouseError, Seq[Row]]
}
