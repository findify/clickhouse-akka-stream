package io.findify.clickhouse.format

import io.findify.clickhouse.format.input.JSONInputFormat
import org.scalatest.{FlatSpec, Matchers}
import sun.nio.cs.StandardCharsets

class JSONInputFormatTest extends FlatSpec with Matchers {
  val dec = new JSONInputFormat()
  it should "decode sample messages" in {
    val example = """{
                    |        "meta":
                    |        [
                    |                {
                    |                        "name": "SearchPhrase",
                    |                        "type": "String"
                    |                },
                    |                {
                    |                        "name": "c",
                    |                        "type": "UInt64"
                    |                }
                    |        ],
                    |
                    |        "data":
                    |        [
                    |                {
                    |                        "SearchPhrase": "",
                    |                        "c": "8267016"
                    |                },
                    |                {
                    |                        "SearchPhrase": "интерьер ванной комнаты",
                    |                        "c": "2166"
                    |                },
                    |                {
                    |                        "SearchPhrase": "яндекс",
                    |                        "c": "1655"
                    |                },
                    |                {
                    |                        "SearchPhrase": "весна 2014 мода",
                    |                        "c": "1549"
                    |                },
                    |                {
                    |                        "SearchPhrase": "фриформ фото",
                    |                        "c": "1480"
                    |                }
                    |        ],
                    |
                    |        "totals":
                    |        {
                    |                "SearchPhrase": "",
                    |                "c": "8873898"
                    |        },
                    |
                    |        "extremes":
                    |        {
                    |                "min":
                    |                {
                    |                        "SearchPhrase": "",
                    |                        "c": "1480"
                    |                },
                    |                "max":
                    |                {
                    |                        "SearchPhrase": "",
                    |                        "c": "8267016"
                    |                }
                    |        },
                    |
                    |        "rows": 5,
                    |
                    |        "rows_before_limit_at_least": 141137
                    |}
                    |""".stripMargin
    val response = dec.read(example.getBytes("UTF-8"))
    response.right.get.size shouldBe 5
  }
}
