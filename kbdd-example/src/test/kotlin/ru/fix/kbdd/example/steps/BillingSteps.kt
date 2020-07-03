package ru.fix.kbdd.example.steps

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.fix.corounit.allure.Step
import ru.fix.kbdd.example.config.Settings
import ru.fix.kbdd.rest.Rest

class BillingSteps : KoinComponent {
    val settings by inject<Settings>()

    @Step
    suspend fun `Withdraw money from customers account`(amount: Int) {
        Rest.request {
            baseUri(settings.baseUri)
            body {
                "amount" {
                    "amount" % amount
                    "currency" % "RUB"
                }
            }
            post("withdraw")
        }
    }
}