package ru.fix.kbdd.example.cases.documentation

import io.qameta.allure.Description
import io.qameta.allure.Epic
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.fix.corounit.allure.Package
import ru.fix.corounit.allure.invoke
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.request
import ru.fix.kbdd.example.TestFramework.makeCodeSnippet

@Epic("Introduction")
@Package("Documentation")
class IntroductionTest : KoinComponent {
    val mockServer by inject<MockServer>()


    @Description("""
        KBDD provides useful classes and methods that simplify test driven development.
        Given example demonstrate simple HTTP interaction with testable application. 
        Code example Allure step contains current test code snippet.
        MockServer is used to setup HTTP response in local HTTP mock server.
    """)
    @Test
    suspend fun `Send HTTP request to application and validate response`() {
        makeCodeSnippet()

        mockServer.`Given server for url answers json`(
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
                """)

        "Send HTTP GET request to a mocked server"{
            request {
                baseUri(mockServer.baseUrl())
                get("/introduction/account/state")
            }
        }

        "Validate HTTP response content from previous request"{
            bodyJson()["status"].isEquals("active")
            bodyJson()["amount"].assert { it.isGreaterThan(100) and it.isLessThanOrEqual(300) }
            bodyJson()["owner"]["firstName"].isEquals("John")
        }
    }
}