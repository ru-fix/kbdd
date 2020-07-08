package ru.fix.kbdd.example.cases

import io.qameta.allure.Description
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Story
import org.codehaus.groovy.runtime.DefaultGroovyMethods.inject
import org.junit.jupiter.api.Test
import org.koin.core.inject
import ru.fix.corounit.allure.invoke
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.example.TestFramework
import ru.fix.kbdd.rest.Rest
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.request

@Epic("Asserts")
@Feature("Standard")
class ResponseAsserts {
    val mockServer by inject<MockServer>()

    @Description("""
        Integer asserts provides basic equality and comparision checks 
    """)
    @Test
    suspend fun `integer asserts`() {
        TestFramework.makeCodeSnippet()
        val url = "/asserts/standard/int/amount"

        mockServer.`Given server for url answers`(
                url,
                """
                {
                    "amount": 120,
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        bodyJson()["amount"].isEquals(120)
        bodyJson()["amount"].isLessThanOrEqual(120)
        bodyJson()["amount"].isLessThanOrEqual(121)
        bodyJson()["amount"].isLessThan(121)
        bodyJson()["amount"].isGreaterThan(119)
        bodyJson()["amount"].isGreaterThanOrEqual(119)
        bodyJson()["amount"].isGreaterThanOrEqual(120)
    }

    @Description("""
        Asserts for floating point numbers looks the same as for integers. 
        Be aware that you can not simply check two floating point numbers for equality 
        and should always provide delta that sets value comparison error limit.
    """)
    @Test
    suspend fun `floating point asserts`() {
        TestFramework.makeCodeSnippet()
        val url = "/asserts/standard/double/indicator"

        mockServer.`Given server for url answers`(
                url,
                """
                {
                    "indicator": 120.5,
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        bodyJson()["indicator"].isEquals(120, 1.0)
        bodyJson()["indicator"].isEquals(120.5, 0.1)
        bodyJson()["indicator"].isLessThanOrEqual(120.5)
        bodyJson()["indicator"].isLessThanOrEqual(121.0)
        bodyJson()["indicator"].isLessThan(121.0)
        bodyJson()["indicator"].isGreaterThan(120.0)
        bodyJson()["indicator"].isGreaterThanOrEqual(120.0)
        bodyJson()["indicator"].isGreaterThanOrEqual(120.5)
    }



}