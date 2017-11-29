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

  implicit val stringEncoder = new StringEncoder()
  implicit val intEncoder = new IntEncoder()
  implicit val boolEncoder = new BooleanEncoder()
  implicit val dateEncoder = new DateEncoder()
  implicit val dateTimeEncoder = new DateTimeEncoder()
  implicit val floatEncoder = new FloatEncoder()
  implicit val doubleEncoder = new DoubleEncoder()
  implicit val longEncoder = new LongEncoder()
  implicit def optionEncoder[T, F <: ScalarField](implicit enc: ScalarEncoder[T,F], s: Scalar[T]): Encoder[Option[T], Nullable[F]] = new OptionEncoder[T,F]()
  implicit def arrayEncoder[T, F <: ScalarField](implicit enc: ScalarEncoder[T,F], s: Scalar[T]): Encoder[Seq[T], CArray[F]] = new ArrayEncoder[T,F]()
  implicit def nestedEncoder[T <: Product, F <: Field](implicit enc: Encoder[T, F]): Encoder[Seq[T], CNested] = new NestedEncoder()

  def deriveEncoder[T](implicit enc: RowEncoder[T]) = enc
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
