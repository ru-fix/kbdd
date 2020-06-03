package ru.fix.kbdd.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import ru.fix.kbdd.json.json
import ru.fix.stdlib.socket.SocketChecker

class AirlineServer {
    private val server = WireMockServer(WireMockConfiguration.options().port(SocketChecker.getAvailableRandomPort()))
    private val mapper = ObjectMapper().registerKotlinModule()

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
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(json {
                            "result" % "success"
                        }))
                )
        )

        server.stubFor(WireMock.post(WireMock.urlEqualTo("/available_xml"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/xml")
                        .withBody("""
                            <request a="val a" b="val b">
                                <dayOfMonth a1="val a1">1</dayOfMonth>
                                <month a2="val a2">Jan</month>
                                <hour>10</hour>
                                <hour>12</hour>
                                <hour>15</hour>
                            </request>
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

}
