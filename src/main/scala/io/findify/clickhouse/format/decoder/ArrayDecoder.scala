package io.findify.clickhouse.format.decoder
import io.findify.clickhouse.format.{Field, Scalar}
import io.findify.clickhouse.format.Field.{CArray, ScalarField}

class ArrayDecoder[T, F <: ScalarField](implicit dec: Decoder[T, F], s: Scalar[T]) extends Decoder[Seq[T], CArray[F]] {
  override def decodeValue: PartialFunction[Field, Seq[T]] = {
    case CArray(values) => values.map(v => dec.decodeValue.lift(v) match {
      case Some(field) => field
      case None => throw new IllegalArgumentException(s"cannot decode $values")
    })
  }
}
