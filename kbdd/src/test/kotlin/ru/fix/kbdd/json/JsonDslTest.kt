package ru.fix.kbdd.json

import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class JsonDslTest {
    @Test
    fun `build json with dsl and serialize to string`() {
        val mapper = ObjectMapper()
        val node = mapper.json {
            "data" {
                "array" % array(1, 2, 3)
                "singleElementArray" % array(1)
                "emptyArray" % array()
                "complexObject" {
                    "string" % "43"
                    "numeric" % 43
                }
                "emptyComplex" {}
                "arrayOfDifferentThings" % array("string", 27, { "name" % "first" }, { "name" % "second" })
                "numeric" % 12
                """escaped"String""" % """m"y"""
                "nullable" % null
                "complexInArray" % array(
                        {
                            "name" % "one"
                            "value" % 1
                        },
                        {
                            "name" % "two"
                            "value" % 2
                        })
                "singleComplexInArray" % array({ "number" % 12 })
                "singleEmptyComplexInArray" % array({})
            }
        }
        mapper.writeValueAsString(node).shouldBe("""""" +
                """{"data":{""" +
                """"array":[1,2,3],""" +
                """"singleElementArray":[1],""" +
                """"emptyArray":[],""" +
                """"complexObject":{""" +
                """"string":"43",""" +
                """"numeric":43},""" +
                """"emptyComplex":{},""" +
                """"arrayOfDifferentThings":["string",27,{"name":"first"},{"name":"second"}],""" +
                """"numeric":12,""" +
                """"escaped\"String":"m\"y",""" +
                """"nullable":null,""" +
                """"complexInArray":[{"name":"one","value":1},{"name":"two","value":2}],""" +
                """"singleComplexInArray":[{"number":12}],""" +
                """"singleEmptyComplexInArray":[{}]""" +
                """}}"""
        )
    }

    @Test
    fun `loop inside template`() {
        val mapper = ObjectMapper()
        val node = mapper.json {
            "data" {
                "arrayOfInts" % (1..2).map { it }
                "arrayOfComplex" % (1..2).map { { "num" % it } }
                (1..2).map {
                    "num$it" % it
                }
                "complex" {
                    (1..2).map {
                        "num$it" % it
                    }
                }
            }
        }
        mapper.writeValueAsString(node).shouldBe("""""" +
                """{"data":{""" +
                """"arrayOfInts":[1,2],""" +
                """"arrayOfComplex":[{"num":1},{"num":2}],""" +
                """"num1":1,""" +
                """"num2":2,""" +
                """"complex":{""" +
                """"num1":1,""" +
                """"num2":2""" +
                """}""" +
                """}}"""
        )
    }

    @Test
    fun `part of json is formed by a builder function with a json dsl argument`() {

        val mapper = ObjectMapper()

        fun builder(child: Json.() -> Unit): JsonNode {
            return mapper.json {
                "data" {
                    "value" % 100
                    "child" % child
                }
            }
        }

        val node = builder {
            "childValue" % 42
        }

        node["data"]["child"]["childValue"].intValue().shouldBe(42)
    }

    @Test
    fun `part of json is formed by a function that returns JsonNode`() {

        val mapper = ObjectMapper()
        fun child(): JsonNode = mapper.createObjectNode().apply {
            put("childValue", 42)
        }

        val node = mapper.json {
            "data" {
                "value" % 100
                "child" % child()
            }
        }
        node["data"]["child"]["childValue"].intValue().shouldBe(42)
    }

    @Test
    fun `quoted big long value to safely pass to frontend`() {
        val mapper = ObjectMapper().configure(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature(), true)

        val node = mapper.json {
            "data" % Long.MAX_VALUE
        }
        mapper.writeValueAsString(node).shouldBe("" +
                """{""" +
                """"data":"9223372036854775807"""" +
                """}"""
        )
    }

    @Test
    fun `part of json structure is strictly specified`() {
        val mapper = ObjectMapper()

        class MyRequest {
            var type = -1
            var data: Json.() -> Unit = {}
        }

        fun doMyRequest(dsl: MyRequest.() -> Unit): String {
            val request = MyRequest().apply(dsl)

            return mapper.writeValueAsString(mapper.json {
                "type" % request.type
                "data" % request.data
            })

        }

        doMyRequest {
            type = 12
            data = {
                "one" % 1
            }
        }.shouldBe("" +
                """{""" +
                """"type":12,""" +
                """"data":{""" +
                """"one":1""" +
                """}}"""
        )
    }
}