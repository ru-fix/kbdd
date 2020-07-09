package ru.fix.kbdd.example.cases.documentation

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.Package
import ru.fix.kbdd.asserts.isContains
import ru.fix.kbdd.asserts.isEquals
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.example.TestFramework.makeCodeSnippet
import ru.fix.kbdd.rest.Rest.bodyString
import ru.fix.kbdd.rest.Rest.request
import ru.fix.kbdd.rest.Rest.statusCode
import ru.fix.kbdd.rest.Rest.statusLine

@Epic("Rest")
@Feature("Json")
@Package("Documentation")
class JsonRestTest : KoinComponent {
    val mockServer by inject<MockServer>()

    @Test
    suspend fun `sending and receiving elements of HTTP response`() {
        val url = "/rest/access-elements"
        mockServer.`Given server for url answers json`(url, "body-data")
        request {
            baseUri(mockServer.baseUrl())
            headers { "my-header" % "my-header-value" }
            post(url)
        }
        statusCode().isEquals(200)
        statusLine().isContains("OK")
        bodyString().isContains("body-data")
    }

    @Test
    suspend fun `post json using json dsl`() {
        makeCodeSnippet()
        val url = "/rest/post-json"

        mockServer.`Given server for url answers json`(
                url,
                """ {"status": "ok"} """)

        request {
            baseUri(mockServer.baseUrl())
            post(url)
            body {
                "request-data" {
                    "number" % 42
                    "string" % "hello"
                    "object" { "content" % "nice" }
                    "array-of-numbers" % array(1, 2, 3)
                    "array-of-objects" % array(
                            { "name" % "first" },
                            { "name" % "second" }
                    )
                }
            }
        }
    }

    @Test
    suspend fun `post json using data object`() {
        makeCodeSnippet()
        val url = "/rest/post-object"

        mockServer.`Given server for url answers json`(
                url,
                """ {"status": "ok"} """)

        data class MyObject(val number: Int, val string: String)

        request {
            baseUri(mockServer.baseUrl())
            post(url)
            body(MyObject(number = 42, string = "hello"))
        }
    }

    @Test
    suspend fun `post json with nulls`() {
        makeCodeSnippet()
        val url = "/rest/json-with-nulls"

        mockServer.`Given server for url answers json`(url, """ {"status": "ok"} """)

        request {
            baseUri(mockServer.baseUrl())
            post(url)
            body {
                "one" % 1
                "two" % null
            }
        }
    }

    @Test
    suspend fun `post json without nulls`() {
        makeCodeSnippet()
        val url = "/rest/json-with-nulls"

        mockServer.`Given server for url answers json`(url, """ {"status": "ok"} """)

        request {
            baseUri(mockServer.baseUrl())
            post(url)
            body (sendNulls = false){
                "one" % 1
                "two" % null
            }
        }
    }

}