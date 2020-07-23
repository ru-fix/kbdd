package ru.fix.kbdd.example.cases.documentation

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.inject
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
    suspend fun `sending and receiving elements of HTTP response`() {
        makeCodeSnippet()

        mockServer.`Given server for url answers cookie`(
                "/introduction/account/state",
                """
                {
                    "status": "active",
                    "amount": 120,
                    "owner": {
                        "firstName": "John",
                        "lastName": "Smith"
                    }
                }
                """,
                "1234")

        "Send HTTP GET request to a mocked server"{
            Rest.request {
                baseUri(mockServer.baseUrl())
                get("/introduction/account/state")
            }
        }

        "Validate HTTP response content from previous request"{
            Rest.bodyJson()["status"].isEquals("active")
            Rest.bodyJson()["amount"].assert { it.isGreaterThan(100) and it.isLessThanOrEqual(300) }
            Rest.bodyJson()["owner"]["firstName"].isEquals("John")
            Rest.cookie()["SESSION"].isEquals("1234")

        }
    }
}