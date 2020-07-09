package ru.fix.kbdd.example.cases.example

import io.kotlintest.shouldBe
import io.qameta.allure.Description
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Story
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.Package
import ru.fix.corounit.allure.invoke
import ru.fix.corounit.allure.parameterized
import ru.fix.corounit.allure.row
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.steps.AirportSteps
import ru.fix.kbdd.example.steps.AirportSteps.Booking
import ru.fix.kbdd.example.steps.BillingSteps
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.bodyXml
import java.time.LocalDate

@Epic("Travel")
@Feature("Flight")
@Package("Example.AriportBooking.For the next day")
class AirportBookingTest : KoinComponent {
    val Airport by inject<AirportSteps>()
    val Billing by inject<BillingSteps>()

    @Story("Flight booking")
    @Description("""
        User successfully purchases a ticket for the tomorrow's flight.
        http://documentation.acme.com/booking.html
        """)

    @Test
    suspend fun `Successfull booking for tomorrow`() {

        var booking = Booking()
        "User make a request to reserve airport ticket for the next date"{
            booking = Airport.`Reserve airport ticket for a flight for date`(
                    reservationDate = LocalDate.now().plusDays(1)
            )
        }

        val expectedPrice = 144_000
        "Reservation price does not exceed $expectedPrice"(booking.price <= expectedPrice)

        "Money withdrawn and user receives bonus miles"{
            Billing.`Withdraw money from customers account`(booking.price)
            bodyJson()["result"].isEquals("success")
            bodyJson()["bonusMiles"].isGreaterThanOrEqual(1)
        }
    }


    @Test
    suspend fun `Flight booking is available for three days (xml)`() {

        Airport.`Check availability for the day (xml)`(1, "Jan")
        val body = bodyXml()
        body.xmlPath("$.dayOfMonth") isEquals 1
        body.xmlPath("$.month") isEquals "Jan"

        body.xmlPath("$.@a") isEquals "val a"
        body.xmlPath("$.@b") isEquals "val b"
        body.xmlPath("$.dayOfMonth.@a1") isEquals "val a1"
        body.xmlPath("$.month.@a2") isEquals "val a2"
        body.xmlPath("$.hour.size()") isEquals 3
        body.xmlPath("$.hour[0]") isEquals 10
        body.xmlPath("$.hour[1]") isEquals 12
        body.xmlPath("$.hour[2]") isEquals 15

        body.xmlPath("$.dayOfMonth").asInt() shouldBe 1
        body.xmlPath("$.month").asString() shouldBe "Jan"
        body.xmlPath("$.@a").asString() shouldBe "val a"
        body.xmlPath("$.@b").asString() shouldBe "val b"
        body.xmlPath("$.dayOfMonth.@a1").asString() shouldBe "val a1"
        body.xmlPath("$.month.@a2").asString() shouldBe "val a2"
        body.xmlPath("$.hour.size()").asInt() shouldBe 3
        body.xmlPath("$.hour[0]").asInt() shouldBe 10
        body.xmlPath("$.hour[1]").asInt() shouldBe 12
        body.xmlPath("$.hour[2]").asInt() shouldBe 15

        body.xmlPath("$.hour.size()").asLong() shouldBe 3L
        body.xmlPath("$.hour[0]").asLong() shouldBe 10L
    }
}