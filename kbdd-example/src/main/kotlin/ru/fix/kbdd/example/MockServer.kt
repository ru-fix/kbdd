package ru.fix.kbdd.example

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ru.fix.corounit.allure.Step
import ru.fix.kbdd.json.Json
import ru.fix.kbdd.json.json
import ru.fix.kbdd.rest.Rest
import ru.fix.stdlib.socket.SocketChecker

class MockServer {
    private val server = WireMockServer(WireMockConfiguration.options().port(SocketChecker.getAvailableRandomPort()))
    private val mapper = jacksonObjectMapper()

    init {
        server.stubFor(WireMock.post(WireMock.urlEqualTo("/book-flight"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(json {
                            "result" % true
                            "price" % 142
                        }))
                )
        )

        server.stubFor(WireMock.post(WireMock.urlEqualTo("/withdraw"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(json {
                            "result" % "success"
                            "bonusMiles" % 2
                        }))
                )

        )

        server.stubFor(WireMock.post(WireMock.urlEqualTo("/available"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/xml")
                        .withBody("""
                            <availability id="43805">
                                <slot>
                                    <month format="Mmm">Jan</month>
                                    <dayOfMonth format="dd">01</dayOfMonth>
                                    <time>1000</time>
                                    <time>1400</time>
                                    <time>1730</time>
                                </slot>
                            </availability>
                            """.trimIndent())
                )
        )
    }

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }

    fun baseUrl() = server.baseUrl()

    @Step
    suspend fun `Given server for url answers json`(url: String, response: String) {
        server.stubFor(WireMock.any(WireMock.urlPathEqualTo(url))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(response))
        )
    }

    @Step
    suspend fun `Given server for url answers xml`(url: String, response: String) {
        server.stubFor(WireMock.any(WireMock.urlPathEqualTo(url))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/xml")
                        .withBody(response))
        )
    }
}
