package ru.fix.kbdd.example.cases

import io.qameta.allure.Epic
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.invoke
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.request
import ru.fix.kbdd.example.TestFramework.makeCodeSnapshot

@Epic("Introduction")
class IntroductionTest : KoinComponent {


    val mockServer by inject<MockServer>()


    @Test
    suspend fun `REST request response`() {
        makeCodeSnapshot()

        mockServer.`Given server for url answers`(
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