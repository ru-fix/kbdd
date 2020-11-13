package ru.fix.kbdd.example.cases.documentation

import com.google.common.net.HttpHeaders
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.Package
import ru.fix.corounit.allure.invoke
import ru.fix.kbdd.asserts.get
import ru.fix.kbdd.asserts.isEquals
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.example.TestFramework
import ru.fix.kbdd.rest.Rest

@Epic("Rest")
@Feature("Redirect")
@Package("Documentation")
class RedirectRestTest : KoinComponent {

    val mockServer by inject<MockServer>()

    @Test
    suspend fun `receiving redirect response`() {
        TestFramework.makeCodeSnippet()

        mockServer.`Given server for url temporary redirects`(
                url = "/introduction/account/state",
                location = mockServer.baseUrl() + "/another/path"
        )

        mockServer.`Given server for url answers json`(
                url = "/another/path",
                response = """{"key": "value"}"""
        )

        "Send HTTP GET request to a mocked server"{
            Rest.request {
                baseUri(mockServer.baseUrl())
                get("/introduction/account/state")
            }
        }

        "Validate HTTP response content from previous request"{
            Rest.statusCode().isEquals(200)
            Rest.bodyJson()["key"].isEquals("value")
        }
    }

    @Test
    suspend fun `receiving redirect response without following`() {
        TestFramework.makeCodeSnippet()

        val redirectUrl = mockServer.baseUrl() + "/another/path"
        mockServer.`Given server for url temporary redirects`(
                url = "/introduction/account/state",
                location = redirectUrl
        )

        "Send HTTP GET request to a mocked server"{
            Rest.request {
                followRedirects(false)
                baseUri(mockServer.baseUrl())
                get("/introduction/account/state")
            }
        }

        "Validate HTTP response content from previous request"{
            Rest.statusCode().isEquals(302)
            Rest.headers()[HttpHeaders.LOCATION].isEquals(redirectUrl)
        }
    }
}