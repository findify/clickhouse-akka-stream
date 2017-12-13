package io.findify.clickhouse.format.input

import akka.util.ByteString
import io.findify.clickhouse.format.ClickhouseError
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.input.InputFormat.Response


trait InputFormat {
  def name: String
  def read(data: ByteString): Either[ClickhouseError, Response]
}

object InputFormat {
  case class Statistics(elapsed: Double, rows_read: Int, bytes_read: Int)
  case class TableMeta(fields: Map[String,String])
  case class Response(meta: TableMeta,  data: List[Row], rows: Int, statistics: Option[Statistics], rowsBeforeLimit: Option[Int])
}