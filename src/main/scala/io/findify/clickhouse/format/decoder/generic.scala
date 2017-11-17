package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.{Field, Scalar}
import io.findify.clickhouse.format.Field._
import org.joda.time.{LocalDate, LocalDateTime}
import shapeless.{HList, HNil, LabelledProductTypeClass, LabelledProductTypeClassCompanion, Lazy}

object generic {
  import Scalar._
  implicit val stringDecoder = new StringDecoder()
  implicit val intDecoder = new IntDecoder()
  implicit val byteDecoder = new ByteDecoder()
  implicit val booleanDecoder = new BooleanDecoder()
  implicit val dateDecoder = new DateDecoder()
  implicit val dateTimeDecoder = new DateTimeDecoder()
  implicit val doubleDecoder = new DoubleDecoder()
  implicit val floatDecoder = new FloatDecoder()
  implicit val longDecoder = new LongDecoder()
  implicit def arrayDecoder[T, F <: ScalarField](implicit dec: Decoder[T, F], s: Scalar[T]): Decoder[Seq[T], CArray[F]] = new ArrayDecoder[T,F]()
  implicit def optionDecoder[T, F <: ScalarField](implicit dec: Decoder[T,F], s: Scalar[T]): Decoder[Option[T], Nullable[F]] = new OptionEncoder[T, F]()

  type RowDecoder[T] = Decoder[T, _ <: Field]
  def deriveDecoder[T](implicit dec: Lazy[RowDecoder[T]]) = dec.value

  object auto extends LabelledProductTypeClassCompanion[RowDecoder] {
    object typeClass extends LabelledProductTypeClass[RowDecoder] {
      override def emptyProduct: RowDecoder[HNil] = new Decoder[HNil, Field] {
        override def decodeValue: PartialFunction[Field, HNil] = ???
        override def decode(xname: String, row: Row): HNil = HNil
      }

      override def product[H, T <: HList](name: String, ch: RowDecoder[H], ct: RowDecoder[T]): RowDecoder[shapeless.::[H, T]] = new Decoder[shapeless.::[H, T], Field] {
        override def decodeValue: PartialFunction[Field, shapeless.::[H, T]] = ???
        override def decode(xname: String, row: Row): shapeless.::[H, T] = {
          ch.decode(name, row) :: ct.decode(name, row)
        }
      }

      override def project[F, G](instance: => RowDecoder[G], to: F => G, from: G => F): RowDecoder[F] = new Decoder[F, Field] {
        override def decodeValue: PartialFunction[Field, F] = ???
        override def decode(xname: String, row: Row): F = {
          from(instance.decode(xname, row))
        }
      }
    }
  }
}
