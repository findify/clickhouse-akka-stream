package io.findify.clickhouse.format.output

import akka.util.ByteString
import io.findify.clickhouse.format.Row

trait OutputFormat {
  def name: String
  def write(value: Row): ByteString
}
