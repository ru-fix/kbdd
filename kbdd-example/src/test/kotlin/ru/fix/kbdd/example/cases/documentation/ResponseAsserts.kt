package ru.fix.kbdd.example.cases.documentation

import io.qameta.allure.Description
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.fix.corounit.allure.Package
import ru.fix.corounit.allure.invoke
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.example.TestFramework
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.request

@Epic("Asserts")
@Feature("Standard")
@Package("Documentation")
class ResponseAsserts: KoinComponent {
    val mockServer by inject<MockServer>()

    @Description("""
        Integer asserts provide basic equality and comparision checks 
    """)
    @Test
    suspend fun `integer asserts`() {
        TestFramework.makeCodeSnippet()
        val url = "/asserts/standard/int/amount"

        mockServer.`Given server for url answers json`(
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

        mockServer.`Given server for url answers json`(
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

    @Description("""
        Asserts for strings validate content. 
        String also can be lexicographically compared to another string.
    """)
    @Test
    suspend fun `string asserts`() {
        TestFramework.makeCodeSnippet()
        val url = "/asserts/standard/string/text"

        mockServer.`Given server for url answers json`(
                url,
                """
                {
                    "text": "Brown little fox with 4 friends",
                    "word": "orange"
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        bodyJson()["text"].isContains("little")
        bodyJson()["text"].isMatches(".*\\d.*")
        bodyJson()["word"].isGreaterThan("apple")
        bodyJson()["word"].isGreaterThanOrEqual("apple")
        bodyJson()["word"].isGreaterThanOrEqual("orange")
        bodyJson()["word"].isEquals("orange")
        bodyJson()["word"].isLessThanOrEqual("orange")
        bodyJson()["word"].isLessThanOrEqual("peach")
        bodyJson()["word"].isLessThan("peach")
    }

    @Description("""
        To simplify assertion logic string values in response can be automatically cast to numbers. 
        Casting relay on asserting method argument. 
        If you compare value from response with number, and value in response is a string, 
        but can be casted to number. Then KBDD will try to automatically cast string value 
        to a numeric type of the argument.
        Test demonstratest difference in comparing string with string and 
        comparing string with numbers with auto-cast in action.
        """)
    @Test
    suspend fun `string values can be auto-casted to numeric values`() {
        TestFramework.makeCodeSnippet()
        val url = "/asserts/standard/string/auto-cast"

        mockServer.`Given server for url answers json`(
                url,
                """
                {
                    "number-as-a-string": "344",
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        bodyJson()["number-as-a-string"].isContains("4")
        bodyJson()["number-as-a-string"].isMatches("\\d+")
        bodyJson()["number-as-a-string"].isGreaterThan("10000")
        bodyJson()["number-as-a-string"].isGreaterThanOrEqual("10000")
        bodyJson()["number-as-a-string"].isGreaterThanOrEqual("344")
        bodyJson()["number-as-a-string"].isEquals("344")
        bodyJson()["number-as-a-string"].isLessThanOrEqual("344")
        bodyJson()["number-as-a-string"].isLessThanOrEqual("6")
        bodyJson()["number-as-a-string"].isLessThan("6")

        bodyJson()["number-as-a-string"].isGreaterThan(2)
        bodyJson()["number-as-a-string"].isGreaterThan(200)
        bodyJson()["number-as-a-string"].isGreaterThan(343)

        bodyJson()["number-as-a-string"].isGreaterThanOrEqual(2)
        bodyJson()["number-as-a-string"].isGreaterThanOrEqual(200)
        bodyJson()["number-as-a-string"].isGreaterThanOrEqual(343)
        bodyJson()["number-as-a-string"].isGreaterThanOrEqual(344)

        bodyJson()["number-as-a-string"].isEquals(344)
        bodyJson()["number-as-a-string"].isLessThanOrEqual(344)
        bodyJson()["number-as-a-string"].isLessThanOrEqual(345)
        bodyJson()["number-as-a-string"].isLessThanOrEqual(600)
        bodyJson()["number-as-a-string"].isLessThan(600)
    }

    @Description("""
        Multiple assert of response subtree can be combained. 
        You can invoke assert block on parent node 
        and access and validate values within parent subtree. 
    """)
    @Test
    suspend fun `asserts can be combined into single assert block`() {
        TestFramework.makeCodeSnippet()
        val url = "/asserts/standard/complex"

        mockServer.`Given server for url answers json`(
                url,
                """
                {
                    "number": 1024,
                    "word": "orange"
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        "single-expression field assert without assert block"{
            bodyJson()["number"].isGreaterThan(100)
        }

        "single-expression field assert iside explicit assert block"{
            bodyJson()["number"].assert {
                it.isGreaterThan(100)
            }
        }

        "single-expression assert with field selector inside explicit assert block"{
            bodyJson().assert {
                it["number"].isGreaterThan(100)
            }
        }

        "multi-expression assert"{
            bodyJson().assert {
                it["number"].isGreaterThan(100) and it["word"].isContains("range")
            }
        }
    }
}