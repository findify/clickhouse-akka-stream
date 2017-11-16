package io.findify.clickhouse.format.decoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.{CArray, Row, ScalarField}
import io.findify.clickhouse.format.decoder.IntDecoder.{Int32Decoder, UInt32Decoder}
import shapeless.{HList, HNil, LabelledProductTypeClass, LabelledProductTypeClassCompanion, Lazy}

object generic {
  implicit val stringDecoder = new StringDecoder()
  implicit val int32Decoder = new Int32Decoder()
  implicit def arrayDecoder[T <: AnyVal, F <: ScalarField](implicit dec: Decoder[T, F]): Decoder[Seq[T], CArray[F]] = new ArrayDecoder[T,F]()

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
