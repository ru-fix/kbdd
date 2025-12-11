package ru.fix.kbdd.example.cases.documentation

import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.matchers.shouldBe
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.fix.corounit.allure.Package
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.rest.Rest
import ru.fix.kbdd.rest.Rest.bodyString
import ru.fix.kbdd.rest.Rest.bodyXml
import ru.fix.kbdd.rest.Rest.request
import ru.fix.kbdd.rest.Rest.statusCode
import ru.fix.kbdd.rest.Rest.statusLine

@Epic("Rest")
@Feature("Xml")
@Package("Documentation")
class XmlRestTest : KoinComponent {
    val mockServer by inject<MockServer>()

    @Test
    suspend fun `sending and receiving elements of HTTP response`() {
        val url = "/rest/xml/available-booking"
        mockServer.`Given server for url answers xml`(url,
                """
                    <response id="765" location="kzn">
                        <dayOfMonth calendar="gregorian">1</dayOfMonth>
                        <month format="Mmm">Jan</month>
                        <hour>10</hour>
                        <hour>12</hour>
                        <hour>15</hour>
                    </response>
                    """)

        request {
            baseUri(mockServer.baseUrl())
            headers { "my-header" % "my-header-value" }
            post(url)
            bodyXml("""
                <request>
                    <timestamp>${System.currentTimeMillis()}</timestamp>
                    <zone>UTC</zone>
                </request>
                """)
        }
        statusCode().isEquals(200)
        statusLine().isContains("OK")

        bodyXml().xmlPath("$.dayOfMonth") isEquals 1
        bodyXml().xmlPath("$.month") isEquals "Jan"
        bodyXml().xmlPath("$.@id") isEquals 765
        bodyXml().xmlPath("$.@location") isEquals "kzn"
        bodyXml().xmlPath("$.dayOfMonth.@calendar") isEquals "gregorian"
        bodyXml().xmlPath("$.month.@format") isEquals "Mmm"
        bodyXml().xmlPath("$.hour.size()") isEquals 3
        bodyXml().xmlPath("$.hour[0]") isEquals 10
        bodyXml().xmlPath("$.hour[1]") isEquals 12
        bodyXml().xmlPath("$.hour[2]") isEquals 15

        bodyXml().xmlPath("$.dayOfMonth").asInt() shouldBe 1
        bodyXml().xmlPath("$.month").asString() shouldBe "Jan"
        bodyXml().xmlPath("$.@id").asInt() shouldBe 765
        bodyXml().xmlPath("$.@id").asString() shouldBe "765"
        bodyXml().xmlPath("$.@location").asString() shouldBe "kzn"
        bodyXml().xmlPath("$.dayOfMonth.@calendar").asString() shouldBe "gregorian"
        bodyXml().xmlPath("$.month.@format").asString() shouldBe "Mmm"
        bodyXml().xmlPath("$.hour.size()").asInt() shouldBe 3
        bodyXml().xmlPath("$.hour.size()").asLong() shouldBe 3L
        bodyXml().xmlPath("$.hour[0]").asInt() shouldBe 10
        bodyXml().xmlPath("$.hour[0]").asLong() shouldBe 10L
        bodyXml().xmlPath("$.hour[1]").asInt() shouldBe 12
        bodyXml().xmlPath("$.hour[2]").asInt() shouldBe 15
    }
}