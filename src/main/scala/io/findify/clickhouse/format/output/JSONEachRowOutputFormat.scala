package io.findify.clickhouse.format.output
import akka.util.ByteString
import io.circe.Json.JString
import io.circe.JsonObject
import io.findify.clickhouse.format.Row
import io.circe.syntax._
import io.circe._

class JSONEachRowOutputFormat extends OutputFormat {
  override def name: String = "JSONEachRow"
  override def write(value: Row): ByteString = {
    val json = Json.fromJsonObject(JsonObject.fromIterable(value.cells.map(cell => cell.name -> Json.fromString(cell.value))))
    ByteString(json.noSpaces) ++ ByteString("\n")
  }
}
