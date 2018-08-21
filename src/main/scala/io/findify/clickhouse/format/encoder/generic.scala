package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.{Field, Scalar}
import io.findify.clickhouse.format.Field._
import org.joda.time.{LocalDate, LocalDateTime}
import shapeless.{HList, HNil, LabelledProductTypeClass, LabelledProductTypeClassCompanion, Lazy}

object generic {
  implicit class AsRow[T <: Product](obj: T) {
    def asRow(implicit enc: Encoder[T, Field]) = Row(enc.encode("", obj))
  }
  import io.findify.clickhouse.format.Scalar._
  type RowEncoder[T] = Encoder[T, _ <: Field]

  implicit val stringEncoder: ScalarEncoder[String, CString] = new StringEncoder()
  implicit val intEncoder: ScalarEncoder[Int, Int32] = new IntEncoder()
  implicit val boolEncoder: ScalarEncoder[Boolean, UInt8] = new BooleanEncoder()
  implicit val dateEncoder: ScalarEncoder[LocalDate, CDate] = new DateEncoder()
  implicit val dateTimeEncoder: ScalarEncoder[LocalDateTime, CDateTime] = new DateTimeEncoder()
  implicit val floatEncoder: ScalarEncoder[Float, Float32] = new FloatEncoder()
  implicit val doubleEncoder: ScalarEncoder[Double, Float64] = new DoubleEncoder()
  implicit val longEncoder: ScalarEncoder[Long, Int64] = new LongEncoder()

  implicit val optionStringEncoder: Encoder[Option[String], Nullable[CString]] = new OptionEncoder()
  implicit val optionIntEncoder: Encoder[Option[Int], Nullable[Int32]] = new OptionEncoder()
  implicit val optionBoolEncoder: Encoder[Option[Boolean], Nullable[UInt8]] = new OptionEncoder()
  implicit val optionDateEncoder: Encoder[Option[LocalDate], Nullable[CDate]] = new OptionEncoder()
  implicit val optionDateTimeEncoder: Encoder[Option[LocalDateTime], Nullable[CDateTime]] = new OptionEncoder()
  implicit val optionFloatEncoder: Encoder[Option[Float], Nullable[Float32]] = new OptionEncoder()
  implicit val optionDoubleEncoder: Encoder[Option[Double], Nullable[Float64]] = new OptionEncoder()
  implicit val optionLongEncoder: Encoder[Option[Long], Nullable[Int64]] = new OptionEncoder()

  implicit def optionEncoder[T, F <: ScalarField](implicit enc: ScalarEncoder[T,F], s: Scalar[T]): Encoder[Option[T], Nullable[F]] = new OptionEncoder[T,F]()
  implicit def arrayEncoder[T, F <: ScalarField](implicit enc: ScalarEncoder[T,F], s: Scalar[T]): Encoder[Seq[T], CArray[F]] = new ArrayEncoder[T,F]()
  implicit def nestedEncoder[T <: Product, F <: Field](implicit enc: Encoder[T, F]): Encoder[Seq[T], CNested] = new NestedEncoder()

  def deriveEncoder[T](implicit enc: RowEncoder[T]): RowEncoder[T] = enc

  object auto extends LabelledProductTypeClassCompanion[RowEncoder] {
    object typeClass extends LabelledProductTypeClass[RowEncoder] {
      override def emptyProduct: RowEncoder[HNil] = new Encoder[HNil, Field] {
        override def encode(name: String, value: HNil): Map[String, Field] = Map.empty[String, Field]
      }

      override def product[H, T <: HList](name: String, ch: RowEncoder[H], ct: RowEncoder[T]): RowEncoder[shapeless.::[H, T]] = new Encoder[shapeless.::[H, T], Field] {
        override def encode(xname: String, value: shapeless.::[H, T]): Map[String, Field] = {
          ch.encode(name, value.head) ++ ct.encode(name, value.tail)
        }

      }

      override def project[F, G](instance: => RowEncoder[G], to: F => G, from: G => F): RowEncoder[F] = new Encoder[F, Field] {
        override def encode(name: String, value: F): Map[String, Field] = {
          instance.encode(name, to(value))
        }
      }
    }
  }
}
