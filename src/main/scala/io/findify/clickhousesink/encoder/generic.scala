package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.field.Field
import shapeless.{:+:, CNil, Coproduct, HList, HNil, LabelledTypeClass, LabelledTypeClassCompanion, Lazy}

object generic {
  implicit val intEncoder = new IntEncoder()
  implicit val stringEncoder = new StringEncoder()
  implicit val longEncoder = new LongEncoder()
  implicit val floatEncoder = new FloatEncoder()
  implicit val doubleEncoder = new DoubleEncoder()

  implicit def arrayEncoder[T <: AnyVal](implicit encoder: Encoder[T]) = new PrimitiveArrayEncoder[T]()
  implicit def seqEncoder[T <: Product](implicit encoder: Encoder[T]) = new SeqEncoder[T]()

  def deriveEncoder[T](implicit encoder: Lazy[Encoder[T]]) = encoder.value

  object auto extends LabelledTypeClassCompanion[Encoder] {
    object typeClass extends LabelledTypeClass[Encoder] {
      override def emptyProduct: Encoder[HNil] = new Encoder[HNil] {
        override def encode(name: String, value: HNil): Seq[Field] = Nil
      }

      override def product[H, T <: HList](name: String, ch: Encoder[H], ct: Encoder[T]): Encoder[shapeless.::[H, T]] = new Encoder[shapeless.::[H, T]] {
        override def encode(xname: String, value: shapeless.::[H, T]): Seq[Field] = ch.encode(name, value.head) ++ ct.encode("empty",value.tail)
      }

      override def project[F, G](instance: => Encoder[G], to: F => G, from: G => F): Encoder[F] = new Encoder[F] {
        override def encode(name: String, value: F): Seq[Field] = instance.encode(name, to(value))
      }

      override def coproduct[L, R <: Coproduct](name: String, cl: => Encoder[L], cr: => Encoder[R]): Encoder[:+:[L, R]] = ???

      override def emptyCoproduct: Encoder[CNil] = ???
    }
  }
}
