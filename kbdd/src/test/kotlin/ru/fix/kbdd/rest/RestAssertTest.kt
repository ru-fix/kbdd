package ru.fix.kbdd.rest

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ru.fix.corounit.allure.AllureStep
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.bodyString
import ru.fix.kbdd.rest.Rest.request
import ru.fix.kbdd.rest.Rest.statusCode
import ru.fix.kbdd.rest.Rest.statusLine
import ru.fix.stdlib.socket.SocketChecker
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestAssertTest {
    private lateinit var server: WireMockServer

    @BeforeAll
    fun beforeAll() {
        server = WireMockServer(SocketChecker.getAvailableRandomPort())
        server.start()
        server.stubFor(WireMock.post(WireMock.urlPathEqualTo("/json-request"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{
                            "result": "success",
                            "data": {
                                "entries": [
                                    {
                                        "name": "one",
                                        "value": 1
                                    },
                                    {
                                        "name": "two",
                                        "value": 2
                                    }
                                ],
                                "dataValue": 56,
                                "fraction": 8079.07941
                            }
                        }""")))
    }

    @AfterAll
    fun afterAll() {
        server.stop()
    }

    @Test
    fun `extract variables from response`() {

        val step = AllureStep()
        runBlocking(step) {
            request {
                baseUri(server.baseUrl())
                body { }
                post("/json-request")
            }

            val stringValue = bodyJson()["result"].asString()
            stringValue.shouldBe("success")

            val intValue = bodyJson()["data"]["dataValue"].asInt()
            intValue.shouldBe(56)

            val bigDecimalValue = bodyJson()["data"]["fraction"].asBigDecimal()
            bigDecimalValue.shouldBe(BigDecimal("8079.07941"))
        }
    }

    @Test
    fun `asserts pass and write themselves to allure report`() {

        val step = AllureStep()
        runBlocking(step) {

            request {
                baseUri(server.baseUrl())
                body { }
                post("/json-request")
            }

            statusCode().isEquals(200)
            statusCode().isLessThan(201)
            statusCode().isLessThanOrEqual(201)
            statusCode().isLessThanOrEqual(200)
            statusCode().isGreaterThan(199)
            statusCode().isGreaterThanOrEqual(199)
            statusCode().isGreaterThanOrEqual(200)

            statusLine().isContains("OK")
            bodyString().isMatches("""(?s).*data.*dataValue.*""")

            bodyJson()["result"].isEquals("success")
            bodyJson()["data"]["dataValue"].isEquals(56)

            bodyJson()["data"]["entries"]
                    .filter {
                        it["name"].isEquals("two") and it["value"].isEquals(2)
                    }.size().isEquals(1)

            bodyJson()["data"]["entries"]
                    .filter {
                        it["name"].isEquals("two") and it["value"].isEquals(2)
                    }[0]["value"].isEquals(2)

            bodyJson()["data"]["entries"]
                    .filter {
                        it["name"].isEquals("two") and it["value"].isEquals(2)
                    }.single()["value"].isEquals(2)
        }

        val children = step.children.toList()
        var i = 0
        children[i++].step.name.shouldBe("statusCode() == 200")
        children[i++].step.name.shouldBe("statusCode() < 201")
        children[i++].step.name.shouldBe("statusCode() <= 201")
        children[i++].step.name.shouldBe("statusCode() <= 200")
        children[i++].step.name.shouldBe("statusCode() > 199")
        children[i++].step.name.shouldBe("statusCode() >= 199")
        children[i++].step.name.shouldBe("statusCode() >= 200")

        children[i++].step.name.shouldBe("statusLine().isContains(OK)")
        children[i++].step.name.shouldBe("bodyString().isMatches(\"(?s).*data.*dataValue.*\")")

        children[i++].step.name.shouldBe("""bodyJson()["result"] == "success"""")
        children[i++].step.name.shouldBe("""bodyJson()["data"]["dataValue"] == 56""")

        children[i++].step.name.shouldBe("""bodyJson()["data"]["entries"]""" +
                """.filter{(it["name"] == "two") and (it["value"] == 2)}""" +
                ".size() == 1")
        children[i++].step.name.shouldBe("""bodyJson()["data"]["entries"]""" +
                """.filter{(it["name"] == "two") and (it["value"] == 2)}""" +
                """[0]["value"] == 2""")

        children[i++].step.name.shouldBe("""bodyJson()["data"]["entries"]""" +
                """.filter{(it["name"] == "two") and (it["value"] == 2)}""" +
                """.single()["value"] == 2""")
    }

    @Test
    fun `asserts work on fractional numbers`() {
        val step = AllureStep()
        runBlocking(step) {

            request {
                baseUri(server.baseUrl())
                body { }
                post("/json-request")
            }

            bodyJson()["data"]["fraction"].isContains("8079.07941")
        }
    }

}