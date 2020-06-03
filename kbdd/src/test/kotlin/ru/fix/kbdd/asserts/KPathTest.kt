package ru.fix.kbdd.asserts

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal class KPathTest {

    @Test
    fun `navigate through elements`() {
        val data = mutableMapOf<String, Any?>(
                "one" to 1,
                "elements" to listOf(
                        "a",
                        "b"
                ),
                "counts" to listOf(
                        mapOf(
                                "name" to "one",
                                "value" to 1
                        ),
                        mapOf(
                                "name" to "two",
                                "value" to 2
                        )
                )
        )
        KPath(data)["one"].asInt().shouldBe(1)
        KPath(data)["elements"][0].asString().shouldBe("a")
        KPath(data)["elements"][1].asString().shouldBe("b")
        KPath(data)["counts"].filter {
            it["value"].isLessThan(2) and it["name"].isEquals("one")
        }.single()["name"].asString().shouldBe("one")

        var errSlot: AssertionError? = null
        try {
            KPath(data)["one"].isEquals(2)
        } catch (err: AssertionError) {
            errSlot = err
        }
        errSlot.shouldNotBeNull()
        errSlot.message.shouldContain("==")
    }


    @Test
    fun `compare string value as numeric type`() {
        val map = ObjectMapper().registerKotlinModule().readValue<Map<*, *>>(
                """
        {
            "status": "success",
            "data": {
                "resultType": "vector",
                "result": [
                    {
                        "metric": {
                            "app": "bookkeeper"
                        },
                        "value": [
                            1583214944.952,
                            "1"
                        ]
                    }
                ]
            }
        }            
        """, Map::class.java)
        KPath(map)["status"].isEquals("success")
        KPath(map)["data"]["resultType"].isContains("vector")
        KPath(map)["data"]["result"].single()["value"][1].isGreaterThan(0)

    }

    @Test
    fun `filter and then access content of filtered entry`() {
        val map = ObjectMapper().registerKotlinModule().readValue("""
                {
                  "schema": {
                    "entities": [
                      {
                        "name": "wallet",
                        "fields": [
                          {
                            "name": "id"
                          },
                          {
                            "name": "balance"
                          }
                        ]
                      },
                      {
                        "name": "transaction",
                        "fields": [
                          {
                            "name": "id"
                          },
                          {
                            "name": "idempotency_key"
                          },
                          {
                            "name": "details"
                          }
                        ]
                      }
                    ]
                  }
                }
                """,
                Map::class.java)

        KPath(map)["schema"]["entities"]
                .filter { it["name"].isEquals("transaction") }.single()["fields"]
                .size().isEquals(3)

        KPath(map)["schema"]["entities"]
                .single { it["name"].isEquals("transaction") }["fields"]
                .size().isEquals(3)

        KPath(map)["schema"]["entities"]
                .single { it["name"].isEquals("wallet") }["fields"]
                .size().isEquals(2)
    }

    @Test
    fun `compare date time object`() {
        val data = mutableMapOf<String, Any?>(
                "localDateTime" to "2020-05-27T00:00:00Z",
                "offsetDateTime" to "2020-05-27T00:00:00+03:00"
        )
        KPath(data)["localDateTime"].asLocalDateTime() shouldBe LocalDateTime.of(2020, 5, 27, 0, 0, 0, 0)
        KPath(data)["localDateTime"].asOffsetDateTime() shouldBe OffsetDateTime.of(2020, 5, 27, 0, 0, 0, 0, ZoneOffset.UTC)
        KPath(data)["offsetDateTime"].asOffsetDateTime(DateTimeFormatter.ISO_DATE_TIME) shouldBe OffsetDateTime.of(2020, 5, 27, 0, 0, 0, 0, ZoneOffset.ofHours(3))
    }

    @Test
    fun `assert null with null`() {
        val data = mutableMapOf<String, Any?>("one" to 1)

        KPath(data)["three"].isEquals(null)
    }

    @Test
    fun `assert integer with different type numbers`() {
        val data = mapOf("a" to 5)

        KPath(data)["a"].isEquals(5.toByte())
        KPath(data)["a"].isEquals(5.toShort())
        KPath(data)["a"].isEquals(5)
        KPath(data)["a"].isEquals(5.toLong())
        KPath(data)["a"].isEquals(5.toFloat())
        KPath(data)["a"].isEquals(5.toDouble())
        KPath(data)["a"].isEquals(BigDecimal(5))
        KPath(data)["a"].isEquals(BigInteger.valueOf(5L))

        KPath(data)["a"].isLessThan(6.toByte())
        KPath(data)["a"].isLessThan(6.toShort())
        KPath(data)["a"].isLessThan(6)
        KPath(data)["a"].isLessThan(6.toLong())
        KPath(data)["a"].isLessThan(6.toFloat())
        KPath(data)["a"].isLessThan(6.toDouble())
        KPath(data)["a"].isLessThan(BigDecimal(6))
        KPath(data)["a"].isLessThan(BigInteger.valueOf(6L))
    }

    @Test
    fun `assert real number with different type numbers`() {
        val data = mapOf("5.0" to 5.0, "5.4" to 5.4)

        KPath(data)["5.0"].isEquals(5.toByte())
        KPath(data)["5.0"].isEquals(5.toShort())
        KPath(data)["5.0"].isEquals(5)
        KPath(data)["5.0"].isEquals(5.toLong())
        KPath(data)["5.4"].isEquals(5.4.toFloat())
        KPath(data)["5.4"].isEquals(5.4)
        KPath(data)["5.4"].isEquals(BigDecimal(5.4))
        KPath(data)["5.0"].isEquals(BigInteger.valueOf(5L))

        KPath(data)["5.4"].isLessThan(6.toByte())
        KPath(data)["5.4"].isLessThan(6.toShort())
        KPath(data)["5.4"].isLessThan(6)
        KPath(data)["5.4"].isLessThan(6.toLong())
        KPath(data)["5.4"].isLessThan(6.1.toFloat())
        KPath(data)["5.4"].isLessThan(6.1)
        KPath(data)["5.4"].isLessThan(BigDecimal(6.1))
        KPath(data)["5.4"].isLessThan(BigInteger.valueOf(6L))
    }

    @Test
    fun `assert string number with different type numbers`() {
        val data = mapOf("5.0" to "5", "5.4" to "5.4")

        KPath(data)["5.0"].isEquals(5.toByte())
        KPath(data)["5.0"].isEquals(5.toShort())
        KPath(data)["5.0"].isEquals(5)
        KPath(data)["5.0"].isEquals(5.toLong())
        KPath(data)["5.4"].isEquals(5.4.toFloat())
        KPath(data)["5.4"].isEquals(5.4)
        KPath(data)["5.0"].isEquals(BigDecimal(5))
        KPath(data)["5.0"].isEquals(BigInteger.valueOf(5L))

        KPath(data)["5.4"].isLessThan(6.toByte())
        KPath(data)["5.4"].isLessThan(6.toShort())
        KPath(data)["5.4"].isLessThan(6)
        KPath(data)["5.4"].isLessThan(6.toLong())
        KPath(data)["5.4"].isLessThan(6.1.toFloat())
        KPath(data)["5.4"].isLessThan(6.1)
        KPath(data)["5.4"].isLessThan(BigDecimal(6.1))
        KPath(data)["5.4"].isLessThan(BigInteger.valueOf(6L))
    }

    @Test
    fun `assert equals with delta`() {
        val data = mapOf("5" to 5, "5.004" to 5.004, "stringNumber" to "10.005")

        KPath(data)["5"].isEquals(5.01, 0.1)
        KPath(data)["5.004"].isEquals(5, 0.01)
        KPath(data)["5.004"].isEquals(5.01, 0.01)
        KPath(data)["5"].isNotEquals(5.1, 0.001)
        KPath(data)["5.004"].isNotEquals(5.01, 0.001)
        KPath(data)["stringNumber"].isEquals(10, 0.01)
        KPath(data)["stringNumber"].isNotEquals(10, 0.001)
    }
}