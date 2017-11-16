package io.findify.clickhouse.format.input

import io.findify.clickhouse.format.ClickhouseError
import io.findify.clickhouse.format.Field.Row


trait InputFormat {
  def read(data: Array[Byte]): Either[ClickhouseError, Seq[Row]]
}
