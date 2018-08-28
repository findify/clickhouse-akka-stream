package io.findify.clickhouse.format.output

import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import io.findify.clickhouse.format.Field.Row

trait OutputFormat extends Serializable with LazyLogging {
  def name: String
  def write(value: Row): ByteString
}
