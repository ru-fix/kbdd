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
import ru.fix.kbdd.asserts.*
import ru.fix.kbdd.example.steps.AirportSteps
import ru.fix.kbdd.example.steps.AirportSteps.Booking
import ru.fix.kbdd.example.steps.BillingSteps
import ru.fix.kbdd.rest.Rest.bodyJson
import ru.fix.kbdd.rest.Rest.bodyXml
import java.time.LocalDate

@Epic("Example")
@Feature("Flight")
@Package("Example.AirportBooking.For the next day")
class AirportBookingTest : KoinComponent {
    val Airport by inject<AirportSteps>()
    val Billing by inject<BillingSteps>()

    @Story("Flight booking with Json Rest")
    @Description("""
        User successfully purchases a ticket for the tomorrow's flight.
        http://documentation.acme.com/booking.html
        """)

    @Test
    suspend fun `Successful booking for tomorrow`() {
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


    @Story("Booking availability with Xml Rest")
    @Test
    suspend fun `Flight booking is available for three days`() {
        Airport.`Check availability for the day`(1, "Jan")

        bodyXml().xmlPath("$.slot.size()") isEquals 1
        bodyXml().xmlPath("$.slot[0].month") isEquals "Jan"
        bodyXml().xmlPath("$.slot[0].dayOfMonth") isEquals 1
        bodyXml().xmlPath("$.slot[0].time.size()") isEquals 3
        bodyXml().xmlPath("$.slot[0].time[0]") isEquals "1000"
        bodyXml().xmlPath("$.slot[0].time[1]") isEquals "1400"
        bodyXml().xmlPath("$.slot[0].time[2]") isEquals "1730"
    }
}