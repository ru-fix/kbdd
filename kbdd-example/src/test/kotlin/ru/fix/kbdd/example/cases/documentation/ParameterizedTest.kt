package ru.fix.kbdd.example.cases.documentation

import io.qameta.allure.Epic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.Package
import ru.fix.corounit.allure.parameterized
import ru.fix.corounit.allure.row
import ru.fix.kbdd.asserts.get
import ru.fix.kbdd.asserts.isEquals
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.example.TestFramework.makeCodeSnippet
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.request

@Epic("Parameterized")
@Package("Documentation")
class ParameterizedTest : KoinComponent {
    val mockServer by inject<MockServer>()

    val url = "/parameterized/success-status"

    @BeforeEach
    suspend fun `setup mock server success response`() {
        mockServer.`Given server for url answers json`("$url", """ {"status": "success"} """)
    }

    @Test
    suspend fun `parameterize test case with 'parameterized' function `() = parameterized(
            row(12, "Jan"),
            row(17, "Feb"),
            row(23, "Mar")
    ) { dayOfMonth, month ->

        makeCodeSnippet()

        request {
            baseUri(mockServer.baseUrl())
            get(url)
            queryParams {
                "dayOfMonth" % "$dayOfMonth"
                "month" % month
            }
        }
        bodyJson()["status"].isEquals("success")
    }
}