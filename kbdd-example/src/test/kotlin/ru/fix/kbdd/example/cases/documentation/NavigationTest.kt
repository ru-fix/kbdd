package ru.fix.kbdd.example.cases.documentation

import io.qameta.allure.Description
import io.qameta.allure.Epic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.Package
import ru.fix.corounit.allure.invoke
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.MockServer
import ru.fix.kbdd.example.TestFramework
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.request

@Epic("Navigation")
@Package("Documentation")
class NavigationTest : KoinComponent {
    val mockServer by inject<MockServer>()

    @Description("""
        Navigation to child by name allowed if currently selected node is a Map        
    """)
    @Test
    suspend fun `navigate to child by name`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/child-by-name"

        mockServer.`Given server for url answers json`(
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

        mockServer.`Given server for url answers json`(
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

        bodyJson()["accounts"][0]["name"].isEquals("John")
        bodyJson()["accounts"][1]["amount"].isEquals(200)

        bodyJson()["accounts"].size().isEquals(2)
    }

    @Description("""
        Arrays provide system field: size.
        Size can be used to validate count of elements within array.         
    """)
    @Test
    suspend fun `arrays size field`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/array-size"

        mockServer.`Given server for url answers json`(
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

        mockServer.`Given server for url answers json`(
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

        mockServer.`Given server for url answers json`(
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

        mockServer.`Given server for url answers json`(
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

        "using 'map' method to access element and convert them to integers"{
            val listOfInts = bodyJson()["accounts"].map { it["amount"].asInt() }
            Assertions.assertEquals(listOfInts, listOf(100, 200))
        }

        "using 'map' method to validate all children"{
            val names = bodyJson()["accounts"].map { it["name"].isContains("J") }
        }

        "using iteration over collection by index"{
            var names = ""
            for (index in 0 until bodyJson()["accounts"].size().asInt()) {
                names += bodyJson()["accounts"][index]["name"].asString()
            }
            Assertions.assertEquals("JohnJane", names)
        }

    }
    @Test
    suspend fun `convert array to a list`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/array-to-list"

        mockServer.`Given server for url answers json`(
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
                    ],
                    "numbers": [42, 43]
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        "Access collections by converting node to Kotlin List of Maps"{
            val accounts = bodyJson()["accounts"].asListOfMaps()
            Assertions.assertEquals("John", accounts[0]["name"])
        }


        "Access collections by converting node to Kotlin List"{
            val numbers = bodyJson()["numbers"].asList<Int>()
            Assertions.assertEquals(42, numbers[0])
        }
    }

    @Test
    suspend fun `convert object to a map`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/array-to-list"

        mockServer.`Given server for url answers json`(
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
                    ],
                    "numbers": [42, 43]
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        val accounts = bodyJson()["accounts"].asList<Map<String,Any?>>()
        Assertions.assertEquals("John", accounts[0]["name"])

        val numbers = bodyJson()["numbers"].asList<Int>()
        Assertions.assertEquals(42, numbers[0])
    }

    data class Account(val name: String, val amount: Int)

    @Description("Function level data classes not supported by jackson." +
            " Use class level or package level data classes.")
    @Test
    suspend fun `convert object to a data class`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/array-to-list"

        mockServer.`Given server for url answers json`(
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
                    ],
                    "numbers": [42, 43]
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        val accounts = bodyJson()["accounts"].asList<Account>()
        Assertions.assertEquals("John", accounts[0].name)

        val secondAccount = bodyJson()["accounts"][1].asObject<Account>()
        Assertions.assertEquals("Jane", secondAccount.name)
    }

    data class MetaInfo(val timestamp: String)
    data class Shop(val id: Long)

    @Test
    suspend fun `absent data can be treated as null`() {
        TestFramework.makeCodeSnippet()
        val url = "/navigation/absent-data-as-null"

        mockServer.`Given server for url answers json`(
            url,
            """
                {
                    "data": {
                            "name": "John",
                            "shop": { "id": 100 }
                    }
                }
                """)

        request {
            baseUri(mockServer.baseUrl())
            get(url)
        }

        Assertions.assertEquals(Shop(100), bodyJson()["data"]["shop"].asObject<Shop>())
        Assertions.assertEquals("John", bodyJson()["data"]["name"].asString())

        Assertions.assertNull(bodyJson()["not-existent"].asObjectOrNull<MetaInfo>())
        Assertions.assertNull(bodyJson()["not"]["existent"].asObjectOrNull<MetaInfo>())
        Assertions.assertNull(bodyJson()["not"]["existent"].asMapOrNull())
        Assertions.assertNull(bodyJson()["not"]["existent"].asListOrNull<MetaInfo>())
        Assertions.assertNull(bodyJson()["not"]["existent"].asListOfMapsOrNull())
        Assertions.assertNull(bodyJson()["not"]["existent"].asStringOrNull())
        Assertions.assertNull(bodyJson()["not"]["existent"].asIntOrNull())
        Assertions.assertNull(bodyJson()["not"]["existent"].asLongOrNull())
        Assertions.assertNull(bodyJson()["not"]["existent"].asLocalDateTimeOrNull())
        Assertions.assertNull(bodyJson()["not"]["existent"].asOffsetDateTimeOrNull())
        Assertions.assertNull(bodyJson()["not"]["existent"].asOffsetDateTimeOrNull())
        Assertions.assertNull(bodyJson()["not"][2]["existent"].asOffsetDateTimeOrNull())
        Assertions.assertNull(bodyJson()["not"].filter { it["value"].isGreaterThan(10) }["existent"].asOffsetDateTimeOrNull())
        Assertions.assertNull(bodyJson()["not"].first { it["value"].isGreaterThan(10) }["existent"].asOffsetDateTimeOrNull())
        Assertions.assertNull(bodyJson()["not"].single { it["value"].isGreaterThan(10) }["existent"].asOffsetDateTimeOrNull())
    }
}