package io.findify.clickhouse.format.input

import akka.util.ByteString
import io.findify.clickhouse.format.ClickhouseError
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.input.InputFormat.Response

import scala.collection.immutable.ListMap

trait InputFormat {
  def name: String
  def read(data: ByteString): Either[ClickhouseError, Response]
}

object InputFormat {
  case class Statistics(elapsed: Double, rows_read: Long, bytes_read: Long)
  case class TableMeta(fields: ListMap[String, String]) {
    def getFieldType(fieldName: String): Option[String] = fields.get(fieldName)
  }
  case class Response(
      meta: TableMeta,
      data: List[Row],
      rows: Int,
      statistics: Option[Statistics],
      rowsBeforeLimit: Option[Int]
  )
}
