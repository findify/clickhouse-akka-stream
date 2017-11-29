package io.findify.clickhouse

import java.io.{File, FileInputStream, FileOutputStream}

import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.output.OutputFormat

import scala.collection.mutable.ArrayBuffer

class FileBuffer[T](table: String, format: OutputFormat, maxRowsInBuffer: Int) {
  var tempFile = File.createTempFile("clickhouse", table)
  var tempStream = new FileOutputStream(tempFile)
  var size: Long = 0
  val passThroughBuffer = ArrayBuffer[T]()

  def append(row: Row): Unit = {
    val bytes = format.write(row).toArray
    tempStream.write(bytes)
    size += 1
  }

  def append(row: Row, passThrough: T): Unit = {
    append(row)
    passThroughBuffer.append(passThrough)
  }

  def passThrough: Seq[T] = passThroughBuffer.toList

  def isFull: Boolean = size >= maxRowsInBuffer
  def isEmpty: Boolean = size == 0
  def stream: Source[ByteString, _] = {
    tempStream.close()
    StreamConverters.fromInputStream(() => new FileInputStream(tempFile))
  }
  def reset = {
    tempFile.delete()
    passThroughBuffer.clear()
    size = 0
    tempFile = File.createTempFile("clickhouse", table)
    tempStream = new FileOutputStream(tempFile)
  }

  def close = {
    tempStream.close()
    tempFile.delete()
  }
}
