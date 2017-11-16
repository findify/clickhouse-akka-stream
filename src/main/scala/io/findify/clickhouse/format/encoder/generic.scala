package io.findify.clickhouse.format.encoder

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.Field.ScalarField
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
  implicit def optionEncoder[T](implicit enc: ScalarEncoder[T]): Encoder[Option[T]] = new OptionEncoder[T]()
  implicit def arrayEncoder[T <: AnyVal](implicit enc: ScalarEncoder[T]): Encoder[Seq[T]] = new ArrayEncoder[T]()
  implicit def arrayStringEncoder(implicit enc: ScalarEncoder[String]): Encoder[Seq[String]] = new ArrayStringEncoder()
  implicit def nestedEncoder[T <: Product](implicit enc: Encoder[T]): Encoder[Seq[T]] = new NestedEncoder[T]()

  def deriveEncoder[T](implicit enc: Lazy[Encoder[T]]) = enc.value

  object auto extends LabelledProductTypeClassCompanion[Encoder] {
    object typeClass extends LabelledProductTypeClass[Encoder] {
      override def emptyProduct: Encoder[HNil] = new Encoder[HNil] {
        override def encodeS(name: String, value: HNil): Map[String, Field.ScalarField] = Map.empty[String, ScalarField]
        override def encode(name: String, value: HNil): Map[String, Field] = Map.empty[String, Field]
      }

      override def product[H, T <: HList](name: String, ch: Encoder[H], ct: Encoder[T]): Encoder[shapeless.::[H, T]] = new Encoder[shapeless.::[H, T]] {
        override def encodeS(xname: String, value: shapeless.::[H, T]): Map[String, ScalarField] = {
          ch.encodeS(name, value.head) ++ ct.encodeS(name, value.tail)
        }
        override def encode(xname: String, value: shapeless.::[H, T]): Map[String, Field] = {
          ch.encode(name, value.head) ++ ct.encode(name, value.tail)
        }
      }

      override def project[F, G](instance: => Encoder[G], to: F => G, from: G => F): Encoder[F] = new Encoder[F] {
        override def encodeS(name: String, value: F): Map[String, ScalarField] = {
          instance.encodeS(name, to(value))
        }
        override def encode(name: String, value: F): Map[String, Field] = {
          instance.encode(name, to(value))
        }
      }
    }
  }
}
