package io.findify.clickhouse.format

import akka.util.ByteString
import io.findify.clickhouse.format.Field._
import io.findify.clickhouse.format.input.InputFormat.Statistics
import io.findify.clickhouse.format.input.{JSONCompactInputFormat, JSONInputFormat}
import org.joda.time.{LocalDate, LocalDateTime}
import org.scalatest.{FlatSpec, Matchers}

class JSONCompactInputFormatTest extends FlatSpec with Matchers {

  val dec = new JSONCompactInputFormat()

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
                    |                ["", "8267016"],
                    |                ["интерьер ванной комнаты", "2166"],
                    |                ["яндекс", "1655"],
                    |                ["весна 2014 мода", "1549"],
                    |                ["фриформ фото", "1480"]
                    |        ],
                    |
                    |        "totals": ["","8873898"],
                    |
                    |        "extremes":
                    |        {
                    |                "min": ["","1480"],
                    |                "max": ["","8267016"]
                    |        },
                    |
                    |        "rows": 5,
                    |
                    |        "rows_before_limit_at_least": 141137
                    |}
                    |""".stripMargin

    val response = dec.read(ByteString(example))
    response.right.get.data.size shouldBe 5
    response.right.get.rowsBeforeLimit shouldBe Some(141137)
  }

  it should "deal with arrays" in {
    val input =
      """
        |{
        |        "meta":
        |        [
        |                {
        |                        "name": "key",
        |                        "type": "String"
        |                },
        |                {
        |                        "name": "strvalues",
        |                        "type": "Array(String)"
        |                },
        |                {
        |                        "name": "intvalues",
        |                        "type": "Array(UInt8)"
        |                },
        |                {
        |                        "name": "floatvalues",
        |                        "type": "Array(Float32)"
        |                },
        |                {
        |                        "name": "longvalues",
        |                        "type": "Array(UInt64)"
        |                }
        |        ],
        |
        |        "data":
        |        [
        |                ["foo", ["bar","baz"], [1,2,3], [0.1,0.2,0.3], ["1","2","3"]]
        |        ],
        |
        |        "rows": 1,
        |
        |        "statistics":
        |        {
        |                "elapsed": 0.000462454,
        |                "rows_read": 1,
        |                "bytes_read": 107
        |        }
        |}
      """.stripMargin
    val response = dec.read(ByteString(input))
    response.right.get.data.size shouldBe 1
  }

  it should "work with nulls" in {
    val input = """{
                  |        "meta":
                  |        [
                  |                {
                  |                        "name": "key",
                  |                        "type": "String"
                  |                },
                  |                {
                  |                        "name": "v",
                  |                        "type": "Nullable(String)"
                  |                }
                  |        ],
                  |
                  |        "data":
                  |        [
                  |                 ["foo", null]
                  |        ],
                  |
                  |        "rows": 1,
                  |
                  |        "statistics":
                  |        {
                  |                "elapsed": 0.000334462,
                  |                "rows_read": 1,
                  |                "bytes_read": 22
                  |        }
                  |}""".stripMargin
    val response = dec.read(ByteString(input))
    response.right.get.data.size shouldBe 1
  }

  it should "work with 64-bit ints" in {
    val input = """{
                  |        "meta":
                  |        [
                  |                {
                  |                        "name": "key",
                  |                        "type": "String"
                  |                },
                  |                {
                  |                        "name": "a",
                  |                        "type": "Int64"
                  |                },
                  |                {
                  |                        "name": "b",
                  |                        "type": "UInt64"
                  |                }
                  |        ],
                  |
                  |        "data":
                  |        [
                  |                ["foo", "123", "456"]
                  |        ],
                  |
                  |        "rows": 1,
                  |
                  |        "statistics":
                  |        {
                  |                "elapsed": 0.000334462,
                  |                "rows_read": 1,
                  |                "bytes_read": 22
                  |        }
                  |}""".stripMargin
    val response = dec.read(ByteString(input))
    response.right.get.data shouldBe List(Row(Map("key" -> CString("foo"), "a" -> Int64(123), "b" -> UInt64(456))))
  }

  it should "work with dates" in {
    val input = """{
                  |        "meta":
                  |        [
                  |                {
                  |                        "name": "key",
                  |                        "type": "String"
                  |                },
                  |                {
                  |                        "name": "a",
                  |                        "type": "Date"
                  |                },
                  |                {
                  |                        "name": "b",
                  |                        "type": "DateTime"
                  |                }
                  |        ],
                  |
                  |        "data":
                  |        [
                  |                ["foo", "2017-01-01", "2017-01-01 00:00:01"]
                  |        ],
                  |
                  |        "rows": 1,
                  |
                  |        "statistics":
                  |        {
                  |                "elapsed": 0.000334462,
                  |                "rows_read": 1,
                  |                "bytes_read": 22
                  |        }
                  |}""".stripMargin
    val response = dec.read(ByteString(input))
    response.right.get.data shouldBe List(Row(Map("key" -> CString("foo"), "a" -> CDate(new LocalDate(2017, 1, 1)), "b" -> CDateTime(new LocalDateTime(2017, 1, 1, 0, 0, 1)))))
    response.right.get.rows shouldBe 1
    response.right.get.statistics shouldBe Some(Statistics(0.000334462, 1, 22))
  }

  it should "correctly decode dates and datetimes" in {
    val input = """{
                  |        "meta":
                  |        [
                  |                {
                  |                        "name": "d",
                  |                        "type": "Date"
                  |                },
                  |                {
                  |                        "name": "dt",
                  |                        "type": "DateTime"
                  |                }
                  |        ],
                  |
                  |        "data":
                  |        [
                  |                ["2017-01-01", "2017-01-01 01:01:01"]
                  |        ],
                  |
                  |        "rows": 1,
                  |
                  |        "statistics":
                  |        {
                  |                "elapsed": 0.000251193,
                  |                "rows_read": 1,
                  |                "bytes_read": 6
                  |        }
                  |}""".stripMargin
    val response = dec.read(ByteString(input)).right.get
    response.data shouldBe List(Row(Map("d" -> CDate(new LocalDate(2017,1,1)), "dt" -> CDateTime(new LocalDateTime(2017,1,1,1,1,1)))))
  }

  it should "decode nullable strings" in {
    val input = """{
                  |        "meta":
                  |        [
                  |                {
                  |                        "name": "search:count",
                  |                        "type": "UInt64"
                  |                },
                  |                {
                  |                        "name": "search:query",
                  |                        "type": "Nullable(String)"
                  |                }
                  |        ],
                  |
                  |        "data":
                  |        [
                  |                ["103", null]
                  |        ],
                  |
                  |        "rows": 1,
                  |
                  |        "rows_before_limit_at_least": 121,
                  |
                  |        "statistics":
                  |        {
                  |                "elapsed": 0.005706054,
                  |                "rows_read": 93841,
                  |                "bytes_read": 5517950
                  |        }
                  |}""".stripMargin
    val response = dec.read(ByteString(input)).right.get
    response.data shouldBe List(Row(Map("search:count" -> UInt64(103), "search:query" -> Nullable(Option.empty[CString]))))
  }
}
