package ru.fix.kbdd.example.cases.documentation

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.fix.corounit.allure.Package
import ru.fix.corounit.allure.invoke
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.example.TestFramework.makeCodeSnippet
import ru.fix.kbdd.rest.Rest

@Epic("Rest")
@Feature("Cookie")
@Package("Documentation")
class CookieRestTest : KoinComponent {
    val mockServer by inject<MockServer>()

    @Test
    suspend fun `receiving cookie from the response`() {
        makeCodeSnippet()

        mockServer.`Given server for url answers cookie`(
                "/rest/cookies",
                """
                {
                    "status": "active",
                }
                """,
                "1234")

        "Send HTTP GET request to a mocked server"{
            Rest.request {
                baseUri(mockServer.baseUrl())
                get("/rest/cookies")
            }
        }

        "Validate HTTP response content from previous request"{
            Rest.bodyJson()["status"].isEquals("active")
            Rest.cookie()["SESSION"].isEquals("1234")

        }
    }
}