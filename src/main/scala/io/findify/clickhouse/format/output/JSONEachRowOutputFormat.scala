package io.findify.clickhouse.format.output
import akka.util.ByteString
import io.circe.Json.JString
import io.circe.JsonObject
import io.circe.syntax._
import io.circe._
import io.findify.clickhouse.format.Field.Row

class JSONEachRowOutputFormat extends OutputFormat {
  override def name: String = "JSONEachRow"
  override def write(value: Row): ByteString = {
    val json = Json.fromJsonObject(JsonObject.fromIterable(value.valueTuple("")))
    //logger.info(json.noSpaces)
    ByteString(json.noSpaces) ++ ByteString("\n")
  }
}
