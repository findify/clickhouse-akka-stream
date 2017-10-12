package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.Field
import shapeless.{:+:, CNil, Coproduct, HList, HNil, LabelledTypeClass, LabelledTypeClassCompanion, Lazy}

object generic {
  implicit val intEncoder = new IntEncoder()
  implicit val stringEncoder = new StringEncoder()
  implicit val longEncoder = new LongEncoder()
  implicit val floatEncoder = new FloatEncoder()
  implicit val doubleEncoder = new DoubleEncoder()

  implicit def arrayEncoder[T <: AnyVal](implicit encoder: ScalarEncoder[T]) = new PrimitiveArrayEncoder[T]()
  implicit def seqEncoder[T <: Product](implicit encoder: Encoder[T]) = new SeqEncoder[T]()
  implicit def optionEncoder[T](implicit encoder: ScalarEncoder[T]) = new OptionEncoder[T]()

  def deriveEncoder[T](implicit encoder: Lazy[Encoder[T]]) = encoder.value

  object auto extends LabelledTypeClassCompanion[Encoder] {
    object typeClass extends LabelledTypeClass[Encoder] {
      override def emptyProduct: Encoder[HNil] = new Encoder[HNil] {
        override def ddl(name: String, mapper: CustomMapper): String = ""
        override def encode(name: String, value: HNil, mapper: CustomMapper): Seq[Field] = Nil
      }

      override def product[H, T <: HList](name: String, ch: Encoder[H], ct: Encoder[T]): Encoder[shapeless.::[H, T]] = new Encoder[shapeless.::[H, T]] {
        override def ddl(xname: String, mapper: CustomMapper): String = {
          val headDDL = ch.ddl(name, mapper)
          val tailDDL = ct.ddl("empty", mapper)
          if (tailDDL.isEmpty)
            headDDL
          else
            headDDL + "," + tailDDL
        }
        override def encode(xname: String, value: shapeless.::[H, T], mapper: CustomMapper): Seq[Field] = ch.encode(name, value.head, mapper) ++ ct.encode("", value.tail, mapper)
      }

      override def project[F, G](instance: => Encoder[G], to: F => G, from: G => F): Encoder[F] = new Encoder[F] {
        override def ddl(name: String, mapper: CustomMapper): String = instance.ddl(name, mapper)
        override def encode(name: String, value: F, mapper: CustomMapper): Seq[Field] = instance.encode(name, to(value), mapper)
      }

      override def coproduct[L, R <: Coproduct](name: String, cl: => Encoder[L], cr: => Encoder[R]): Encoder[:+:[L, R]] = ???

      override def emptyCoproduct: Encoder[CNil] = ???
    }
  }
}
