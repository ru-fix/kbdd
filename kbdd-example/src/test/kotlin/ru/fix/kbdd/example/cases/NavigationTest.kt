package ru.fix.kbdd.example.cases

import io.qameta.allure.Description
import io.qameta.allure.Epic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.invoke
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.example.TestFramework
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.request

@Epic("Navigation")
class NavigationTest : KoinComponent {
    val mockServer by inject<MockServer>()

    @Description("""
        Navigation to child by name allowed if currently selected node is a Map        
    """)
    @Test
    suspend fun `navigate to child by name`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/child-by-name"

        mockServer.`Given server for url answers`(
                url,
                """
                {
                    "account": {
                        "name": "John",
                        "amount": 100
                    }
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        bodyJson()["account"]["name"].isEquals("John")
        bodyJson()["account"]["amount"].isEquals(100)
    }

    @Description("""
        Navigation to child by index allowed if currently selected node is an Array        
    """)
    @Test
    suspend fun `navigate to child by index`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/child-by-index"

        mockServer.`Given server for url answers`(
                url,
                """
                {
                    "accounts": [
                        {
                            "name": "John",
                            "amount": 100
                        },
                        {
                            "name": "Jane",
                            "amount": 200
                        }
                    ]
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        bodyJson()["account"][0]["name"].isEquals("Jane")
        bodyJson()["account"][1]["amount"].isEquals(200)

        bodyJson()["account"].size().isEquals(2)
    }

    @Description("""
        Arrays provide system field: size.
        Size can be used to validate count of elements within array.         
    """)
    @Test
    suspend fun `arrays size field`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/array-size"

        mockServer.`Given server for url answers`(
                url,
                """
                {
                    "accounts": [
                        {
                            "name": "John",
                            "amount": 100
                        },
                        {
                            "name": "Jane",
                            "amount": 200
                        }
                    ]
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }


        bodyJson()["accounts"].size().isEquals(2)
        bodyJson()["accounts"][0]["amount"].isEquals(100)
        bodyJson()["accounts"][1]["amount"].isEquals(200)

    }


    @Description("""
        Array items can be filtered during navigation.
        Use 'single' method if you expect single result after filtering
    """)
    @Test
    suspend fun `navigate by filtering`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/filtering"

        mockServer.`Given server for url answers`(
                url,
                """
                {
                    "accounts": [
                        {
                            "name": "John",
                            "amount": 100
                        },
                        {
                            "name": "Jane",
                            "amount": 200
                        }
                    ]
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        bodyJson()["accounts"].filter { it["amount"].isGreaterThan(100) }.size().isEquals(1)
        bodyJson()["accounts"].filter { it["amount"].isGreaterThan(100) }[0]["name"].isEquals("Jane")

        "If you expecting only single value returned by filter, you can use 'single' instead of 'filter'"{
            bodyJson()["accounts"].single { it["amount"].isLessThan(150) }["name"].isEquals("John")
        }
    }

    @Description("""
        Singleton arrays support 'single' navigation method         
    """)
    @Test
    suspend fun `navigate to element within Array that contains single element`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/singleton-array"

        mockServer.`Given server for url answers`(
                url,
                """
                {
                    "accounts": [
                        {
                            "name": "John",
                            "amount": 100
                        }
                    ]
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        bodyJson()["accounts"].single()["name"].isEquals("John")
    }

    @Description("""
        Arrays support map method that get you access to each of the itmes
    """)
    @Test
    suspend fun `use map to process each elements of array`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/map"

        mockServer.`Given server for url answers`(
                url,
                """
                {
                    "accounts": [
                        {
                            "name": "John",
                            "amount": 100
                        },
                        {
                            "name": "Jane",
                            "amount": 200
                        }
                    ]
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        "using 'map' method to access all children"{
            val names = bodyJson()["accounts"].map { it["name"].asString() }.joinToString (separator = "")
            Assertions.assertEquals("JohnJane", names)
        }

        "using iteration over collection by index"{
            var names = ""
            for (index in 0 until bodyJson()["accounts"].size().asInt()) {
                names += bodyJson()["accounts"][index]["name"].asString()
            }
            Assertions.assertEquals("JohnJane", names)
        }

    }
}