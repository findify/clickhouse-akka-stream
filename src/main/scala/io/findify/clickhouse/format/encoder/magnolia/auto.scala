package io.findify.clickhouse.format.encoder.magnolia

import io.findify.clickhouse.format.Field
import io.findify.clickhouse.format.encoder.Encoder
import io.findify.clickhouse.format.encoder.generic.RowEncoder
import magnolia._

import language.experimental.macros

object auto {
  type Typeclass[T] = RowEncoder[T]

  def combine[T](ctx: CaseClass[RowEncoder, T]): RowEncoder[T] = new Encoder[T, Field] {
    override def encode(name: String, value: T): Seq[(String, Field)] = {
      ctx.parameters.flatMap(param => {
        param.typeclass.encode(param.label, param.dereference(value))
      })
    }
  }

  def dispatch[T](ctx: SealedTrait[RowEncoder, T])(): RowEncoder[T] = new Encoder[T, Field] {
    override def encode(name: String, value: T): Seq[(String, Field)] = {
      Seq.empty
    }
  }

  implicit def gen[T]: RowEncoder[T] = macro Magnolia.gen[T]

}
