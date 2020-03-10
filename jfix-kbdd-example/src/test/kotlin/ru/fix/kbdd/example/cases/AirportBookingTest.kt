package ru.fix.kbdd.example.cases

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
import ru.fix.kbdd.asserts.get
import ru.fix.kbdd.asserts.isEquals
import ru.fix.kbdd.asserts.isGreaterThanOrEqual
import ru.fix.kbdd.example.CorounitConfig
import ru.fix.kbdd.example.config.Settings
import ru.fix.kbdd.example.steps.AirportSteps
import ru.fix.kbdd.example.steps.AirportSteps.Booking
import ru.fix.kbdd.example.steps.BillingSteps
import ru.fix.kbdd.rest.Rest.bodyJson
import java.time.LocalDate

@Epic("Travel")
@Feature("Flight")
class AirportBookingTest : KoinComponent {

    companion object {
        val my = CorounitConfig
    }

    val Airport by inject<AirportSteps>()
    val Billing by inject<BillingSteps>()
    val settings by inject<Settings>()

    @Story("Flight booking")
    @Description("""
        User successfully purchases a ticket for the tomorrow's flight.
        http://documentation.acme.com/booking.html
        """)
    @Package("Flights.Booking.For the next day.Successful")
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
    suspend fun `Flight booking is available for three days`() = parameterized(
            row(1, "Jan"),
            row(2, "Feb"),
            row(3, "Mar")) { dayOfMonth, month ->

        Airport.`Check availability for the day`(dayOfMonth, month)
        bodyJson()["result"].isEquals("success")
    }
}