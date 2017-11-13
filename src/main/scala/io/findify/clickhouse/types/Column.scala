package io.findify.clickhouse.types

sealed trait Column

case class StringCol(value: String) extends Column
case class UInt32(value: Int) extends Column
case class UInt64(value: Long) extends Column
case class Float32(value: Float) extends Column
case class Float64(Value: Double) extends Column
case class ArrayCol[T](values: List[T]) extends Column