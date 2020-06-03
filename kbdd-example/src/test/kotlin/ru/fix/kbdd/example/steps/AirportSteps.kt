package ru.fix.kbdd.example.steps

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.Step
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.config.Settings
import ru.fix.kbdd.rest.Rest
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.statusCode
import java.time.LocalDate

open class AirportSteps : KoinComponent {
    private val settings by inject<Settings>()

    data class Booking(
            val price: Int = -1
    )

    @Step
    suspend fun `Reserve airport ticket for a flight for date`(reservationDate: LocalDate): Booking {
        Rest.request {
            baseUri(settings.airportBaseUri)
            post("/book-flight")
            body {
                "booking" {
                    "user" % "Smith"
                    "date" % reservationDate
                }
            }
        }
        statusCode().isEquals(200)
        bodyJson()["error"].isNull()
        bodyJson()["price"].isGreaterThanOrEqual(0)

        return Booking(price = bodyJson()["price"].asInt())
    }

    @Step
    suspend fun `Check availability for the day`(dayOfMonth: Int, month: String) {
        Rest.request {
            baseUri(settings.airportBaseUri)
            post("/available")
            body {
                "dayOfMonth" % dayOfMonth
                "month" % month
            }
        }
        bodyJson()["error"].isNull()
    }

    open suspend fun `Check availability for the day (xml)`(dayOfMonth: Int, month: String) {
        Rest.request {
            baseUri(settings.airportBaseUri)
            post("/available_xml")
            bodyXml("""
                <request>
                    <dayOfMonth>$dayOfMonth</dayOfMonth>
                    <month>$month</month>
                </request>
            """.trimIndent())
        }
    }
}