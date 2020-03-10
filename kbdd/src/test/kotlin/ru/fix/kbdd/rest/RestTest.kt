package ru.fix.kbdd.rest

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotlintest.matchers.string.shouldContain
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.bodyString
import ru.fix.kbdd.rest.Rest.request
import ru.fix.kbdd.rest.Rest.statusCode
import ru.fix.kbdd.rest.Rest.statusLine
import ru.fix.stdlib.socket.SocketChecker

private val log = KotlinLogging.logger { }

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestTest {
    private lateinit var server: WireMockServer

    suspend fun beforeAll() {
        server = WireMockServer(SocketChecker.getAvailableRandomPort())
        server.start()

        for(path in listOf(
                "/json-post-request",
                "/json-post-from-string-request")) {

            server.stubFor(post(urlPathEqualTo(path))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{
                            "data":{
                                "entries":[
                                    {
                                        "name":"one",
                                        "value":1
                                    },
                                    {
                                        "name":"two",
                                        "value":2
                                    }],
                                "result":56
                                }
                            }""")))
        }

        for(path in listOf(
                "/post-form-data-request",
                "/json-post-without-nulls",
                "/json-post-with-nulls",
                "/json-post-dto-as-part-of-json-dsl",
                "/json-post-dto-object-in-body",
                "/json-post-dto-with-nulls",
                "/json-post-dsl-dto-with-nulls",
                "/json-post-dsl-dto-without-nulls",
                "/json-post-dto-without-nulls")){

            server.stubFor(post(urlPathEqualTo(path))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{
                                "status":"success"
                            }""")))


        }
    }

    suspend fun afterAll() {
        server.stop()
    }


    @Test
    suspend fun `post json request`() {
        request {
            headers(mapOf("my-header1" to "header-value1"))
            headers { "my-header2" % "header-value2" }

            baseUri(server.baseUrl())

            queryParams(mapOf("queryParam10" to "10"))
            queryParams { "queryParam11" % "11" }

            body {
                "data" {
                    "entries" % array(
                            {
                                "name" % "one"
                                "value" % 1
                            },
                            {
                                "name" % "two"
                                "value" % 2
                            }
                    )

                }
            }
            post("/json-post-request")

        }

        statusCode().isEquals(200)
        statusLine().isContains("OK")
        bodyString().isMatches("(?s).*data.*result.*")
        bodyJson()["data"]["entries"].filter {
            it["name"].isEquals("two")
        }.single()["value"].isEquals(2)

        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-request"))
                        .withQueryParam("queryParam10", equalTo("10"))
                        .withQueryParam("queryParam11", equalTo("11"))
                        .withHeader("my-header1", equalTo("header-value1"))
                        .withHeader("my-header2", equalTo("header-value2"))
        )


        try {

            bodyJson()["data"]["entries"]
                    .first { it["value"].isEquals(2) }["name"]
                    .isEquals("one")
            fail("there should be an assertion error, but was none")
        } catch (err: AssertionError) {
            err.message.shouldContain("first")
        }
    }

    @Test
    suspend fun `post json from string request`() {
        val json = """
                    {
                        "object": "test"
                    }
                """

        request {
            headers(mapOf("my-header1" to "header-value1"))
            headers { "my-header2" % "header-value2" }

            baseUri(server.baseUrl())

            queryParams(mapOf("queryParam10" to "10"))
            queryParams { "queryParam11" % "11" }

            body(json)
            post("/json-post-from-string-request")

        }

        statusCode().isEquals(200)
        statusLine().isContains("OK")

        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-from-string-request"))
                        .withQueryParam("queryParam10", equalTo("10"))
                        .withQueryParam("queryParam11", equalTo("11"))
                        .withHeader("my-header1", equalTo("header-value1"))
                        .withHeader("my-header2", equalTo("header-value2"))
                        .withHeader("Content-Type", containing("application/json"))
                        .withRequestBody(equalToJson(json))
        )
    }

    @Test
    suspend fun `post form data request`() {
        request {
            headers { "my-header" % "header-value" }
            baseUri(server.baseUrl())
            post("/post-form-data-request")
            formParams(mapOf("formParam10" to "10"))
            formParams { "formParam11" % "11" }
        }

        statusCode().isEquals(200)
        bodyJson()["status"].isEquals("success")

        server.verify(
                postRequestedFor(urlPathEqualTo("/post-form-data-request"))
                        .withRequestBody(containing("formParam10=10"))
                        .withRequestBody(containing("formParam11=11"))
                        .withHeader("my-header", equalTo("header-value"))
        )
    }

    @Test
    suspend fun `post dto object in body`() {

        data class MyDto(val number: Int, val text: String)

        request {
            baseUri(server.baseUrl())
            post("/json-post-dto-object-in-body")
            body(MyDto(number = 1, text = "one"))
        }
        statusCode().isEquals(200)

        val json = """
            {
                "number": 1,
                "text": "one"
            }
            """
        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-dto-object-in-body"))
                        .withRequestBody(equalToJson(json))
        )
    }

    @Test
    suspend fun `post dto object as part of json dsl`() {

        data class MyDto(val number: Int, val text: String)

        request {
            baseUri(server.baseUrl())
            post("/json-post-dto-as-part-of-json-dsl")
            body {
                "data" % MyDto(number = 1, text = "one")
            }
        }
        statusCode().isEquals(200)

        val json = """
            {
                "data": {
                    "number": 1,
                    "text": "one"
                }
            }
            """
        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-dto-as-part-of-json-dsl"))
                        .withRequestBody(equalToJson(json))
        )
    }


    @Test
    suspend fun `post json with nulls`() {

        request {
            baseUri(server.baseUrl())
            post("/json-post-with-nulls")
            body {
                "one" % 1
                "two" % null
            }
        }
        statusCode().isEquals(200)
        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-with-nulls"))
                        .withRequestBody(equalToJson("""
                            {
                                "one": 1,
                                "two": null
                            }
                            """))
        )
    }


    @Test
    suspend fun `post json without nulls`() {

        request {
            baseUri(server.baseUrl())
            post("/json-post-without-nulls")
            body (sendNulls = false) {
                "one" % 1
                "two" % null
            }
        }
        statusCode().isEquals(200)
        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-without-nulls"))
                        .withRequestBody(equalToJson("""
                            {
                                "one": 1
                            }
                            """))
        )
    }

    @Test
    suspend fun `post dto without nulls`() {

        data class MyObject(val foo: String?, val bar: String?)

        request {
            baseUri(server.baseUrl())
            post("/json-post-dto-without-nulls")
            body (sendNulls = false) {
                "myObject" % MyObject("foo", null)
            }
        }
        statusCode().isEquals(200)
        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-dto-without-nulls"))
                        .withRequestBody(equalToJson("""
                            {
                                "myObject": {
                                    "foo": "foo"
                                }
                            }
                            """))
        )
    }

    @Test
    suspend fun `post dto with nulls`() {

        data class MyObject(val foo: String?, val bar: String?)

        request {
            baseUri(server.baseUrl())
            post("/json-post-dto-with-nulls")
            body (MyObject("foo", null))
        }
        statusCode().isEquals(200)
        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-dto-with-nulls"))
                        .withRequestBody(equalToJson("""
                            {
                                "foo": "foo",
                                "bar": null
                            }
                            """))
        )
    }

    @Test
    suspend fun `post dsl dto with nulls`() {

        data class MyObject(val foo: String?, val bar: String?)

        request {
            baseUri(server.baseUrl())
            post("/json-post-dsl-dto-with-nulls")
            body {
                "one" % 1
                "two" % null
                "myObject" % MyObject("foo", null)
            }
        }
        statusCode().isEquals(200)
        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-dsl-dto-with-nulls"))
                        .withRequestBody(equalToJson("""
                            {
                                "one": 1,
                                "two": null,
                                "myObject": {
                                    "foo": "foo",
                                    "bar": null
                                }
                            }
                            """))
        )
    }

    @Test
    suspend fun `post dsl dto without nulls`() {

        data class MyObject(val foo: String?, val bar: String?)

        request {
            baseUri(server.baseUrl())
            post("/json-post-dsl-dto-without-nulls")
            body(sendNulls = false) {
                "one" % 1
                "two" % null
                "myObject" % MyObject("foo", null)
            }
        }
        statusCode().isEquals(200)
        server.verify(
                postRequestedFor(urlPathEqualTo("/json-post-dsl-dto-without-nulls"))
                        .withRequestBody(equalToJson("""
                            {
                                "one": 1,
                                "myObject": {
                                    "foo": "foo"
                                }
                            }
                            """))
        )
    }
}