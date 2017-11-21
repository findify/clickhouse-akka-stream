package io.findify.clickhouse.format

import io.circe.{Json, JsonDouble, JsonObject}
import io.findify.clickhouse.format.decoder.Decoder
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, LocalDateTime}

import scala.collection.mutable

sealed trait Field {
  def valueTuple(name: String): Seq[(String, Json)]
  def value: Json
}



object Field {
  sealed trait ScalarField extends Field {
    def valueTuple(name: String): Seq[(String, Json)] = List(name -> value)
  }
  private val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
  private val dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  case class Row(fields: Map[String, Field]) extends Field {
    def as[T](implicit dec: Decoder[T, Field]) = dec.decode("", this)
    override def valueTuple(name: String): Seq[(String, Json)] = fields.toList.flatMap {
      case (fieldName, nested: CNested) => nested.valueTuple(fieldName)
      case (fieldName, other) => other.valueTuple(fieldName)
    }
    override def value: Json = ???
  }
  case class ScalarRow(fields: Map[String, Field]) extends ScalarField {
    override def valueTuple(name: String): Seq[(String, Json)] = ???

    override def value: Json = ???
  }
  case class CString(raw: String) extends ScalarField {
    override def value: Json = Json.fromString(raw)
  }
  case class FixedString(raw: String, length: Int) extends ScalarField {
    override def value: Json = Json.fromString(raw)
  }
  sealed trait ByteNumber extends ScalarField
  sealed trait IntNumber extends ScalarField
  sealed trait LongNumber extends ScalarField
  sealed trait RealNumber extends ScalarField
  case class Int8(raw: Byte) extends ByteNumber {
    override def value: Json = Json.fromInt(raw)
  }
  case class UInt8(raw: Byte) extends ByteNumber {
    override def value: Json = Json.fromInt(raw)
  }
  case class Int32(raw: Int) extends IntNumber {
    override def value: Json = Json.fromInt(raw)
  }
  case class Int64(raw: Long) extends LongNumber {
    override def value: Json = Json.fromLong(raw)
  }
  case class UInt32(raw: Int) extends IntNumber {
    override def value: Json = Json.fromInt(raw)
  }
  case class UInt64(raw: Long) extends LongNumber {
    override def value: Json = Json.fromLong(raw)
  }
  case class CDate(raw: LocalDate) extends ScalarField {
    override def value: Json = Json.fromString(dateFormat.print(raw))
  }
  case class CDateTime(raw: LocalDateTime) extends ScalarField {
    override def value: Json = Json.fromString(dateTimeFormat.print(raw))
  }
  case class Float32(raw: Float) extends ScalarField {
    override def value: Json = Json.fromFloatOrNull(raw)
  }
  case class Float64(raw: Double) extends ScalarField {
    override def value: Json = Json.fromDoubleOrNull(raw)
  }
  case class CNested(raw: Seq[ScalarRow]) extends Field {
    override def value: Json = ???
    override def valueTuple(name: String): Seq[(String, Json)] = {
      for {
        head <- raw.headOption.toList
        fieldName <- head.fields.keys
      } yield {
        val values = for {
          row <- raw
          fieldValue <- row.fields.get(fieldName)
        } yield {
          fieldValue.value
        }
        s"$name.$fieldName" -> Json.fromValues(values)
      }
    }
  }
  case class CArray[T <: ScalarField](raw: Seq[T]) extends ScalarField {
    override def value: Json = Json.fromValues(raw.map(_.value))
  }
  case class Nullable[T <: ScalarField](raw: Option[T]) extends ScalarField {
    override def value: Json = raw match {
      case Some(in) => in.value
      case None => Json.Null
    }
  }
  val fixedStringPattern = "FixedString\\(([0-9]+)\\)".r
  val arrayPattern = "Array\\(([0-9a-zA-Z\\(\\)]+)\\)".r
  val nullablePattern = "Nullable\\(([0-9a-zA-Z\\(\\)]+)\\)".r
  def apply[T](tpe: String, value: Json): Field = (tpe, value.asArray) match {
    case (arrayPattern(subType), Some(items)) => CArray(items.map(item => Field.applyScalar(subType, item)))
    case (nullablePattern(subType), _) => if (value.isNull) Nullable(None) else Nullable(Some(Field.applyScalar(subType, value)))
    case _ => applyScalar(tpe, value)
  }

  def applyScalar[T](tpe: String, value: Json): ScalarField = (tpe, value.asString, value.asArray, value.asNumber) match {
    case (fixedStringPattern(length), Some(str), _, _) => FixedString(str, length.toInt)
    case ("String", Some(str), _, _) => CString(str)
    case ("UInt8", _, _, Some(num)) => UInt8(num.truncateToByte)
    case ("Int8", _, _, Some(num)) => Int8(num.truncateToByte)
    case ("UInt32", _, _, Some(num)) => UInt32(num.truncateToInt)
    case ("Int32", _, _, Some(num)) => Int32(num.truncateToInt)
    case ("UInt64", Some(str), _, _) => UInt64(str.toLong)
    case ("Int64", Some(str), _, _) => Int64(str.toLong)
    case ("Float32", _, _, Some(num)) => Float32(num.toDouble.floatValue())
    case ("Float64", _, _, Some(num)) => Float64(num.toDouble)
    case ("DateTime", Some(str), _, _) => CDateTime(LocalDateTime.parse(str, dateTimeFormat))
    case ("Date", Some(str), _, _) => CDate(LocalDate.parse(str, dateFormat))
  }
}
