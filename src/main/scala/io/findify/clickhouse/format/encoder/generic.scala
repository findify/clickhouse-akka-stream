package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field._
import org.joda.time.{LocalDate, LocalDateTime}
import shapeless.{HList, HNil, LabelledProductTypeClass, LabelledProductTypeClassCompanion, Lazy}

object generic {
  implicit val stringEncoder = new StringEncoder()
  implicit val intEncoder = new IntEncoder()
  implicit val boolEncoder = new BooleanEncoder()
  implicit val dateEncoder = new DateEncoder()
  implicit val dateTimeEncoder = new DateTimeEncoder()
  implicit val floatEncoder = new FloatEncoder()
  implicit val longEncoder = new LongEncoder()
  implicit def optionEncoder[T, F <: ScalarField](implicit enc: ScalarEncoder[T,F]): Encoder[Option[T], Nullable[F]] = new OptionEncoder()
  implicit def arrayEncoder[T <: AnyVal, F <: ScalarField](implicit enc: ScalarEncoder[T,F]): Encoder[Seq[T], CArray[F]] = new ArrayEncoder()
  implicit def arrayStringEncoder(implicit enc: ScalarEncoder[String, CString]): Encoder[Seq[String], CArray[CString]] = new ArrayStringEncoder()
  implicit def nestedEncoder[T <: Product, F <: Field](implicit enc: Encoder[T, F]): Encoder[Seq[T], CNested] = new NestedEncoder()

  def deriveEncoder[T](implicit enc: Lazy[RowEncoder[T]]) = enc.value
  type RowEncoder[T] = Encoder[T, _ <: Field]

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
