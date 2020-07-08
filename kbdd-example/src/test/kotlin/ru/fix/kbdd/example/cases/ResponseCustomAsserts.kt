package ru.fix.kbdd.example.cases

import io.qameta.allure.Description
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.invoke
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.example.TestFramework
import ru.fix.kbdd.rest.Rest
import ru.fix.kbdd.rest.Rest.bodyJson

@Epic("Asserts")
@Feature("Custom")
class ResponseCustomAsserts : KoinComponent {
    val mockServer by inject<MockServer>()

    @Description("""
        You can write custom asserts by providing extension function. 
        If you do so, maybe your asserts is worth to be added to main asserts list into KBDD project. 
        Pull Requests are welcome!
    """)
    @Test
    suspend fun `provide custom asserts`() {
        TestFramework.makeCodeSnippet()
        val url = "/asserts/custom/int/amount"

        mockServer.`Given server for url answers`(
                url,
                """
                {
                    "amount": 120,
                }
                """)

        Rest.request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        fun Checkable.isEvenInteger() = express { source ->
            object : Expression {
                override fun print(): String = "${source.print()} isEvenInteger"
                override fun evaluate(): Boolean = source.evaluate().let { it is Int && it % 2 == 0 }
            }
        }

        "custom assert via extension method"{
            bodyJson()["amount"].isEvenInteger()
        }

        "custom assert without extension method"{
            val intValue  = bodyJson()["amount"].asInt()
            Assertions.assertTrue(intValue % 2 == 0, "isEvenInteger, actual: $intValue")
        }
    }
}