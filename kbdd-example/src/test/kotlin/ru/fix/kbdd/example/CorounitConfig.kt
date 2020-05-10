package ru.fix.kbdd.example

import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.fix.corounit.allure.createStepClassInstance
import ru.fix.corounit.engine.CorounitPlugin
import ru.fix.kbdd.example.config.Settings
import ru.fix.kbdd.example.steps.AirportSteps
import ru.fix.kbdd.example.steps.BillingSteps
import ru.fix.kbdd.rest.Rest
import kotlin.coroutines.CoroutineContext

object CorounitConfig : CorounitPlugin {

    private val airlineServer = AirlineServer()

    init {
        Rest.threadPoolSize = 10
    }

    override suspend fun beforeAllTestClasses(globalContext: CoroutineContext): CoroutineContext {
        airlineServer.start()

        val settings = Settings()
        settings.airportBaseUri = airlineServer.baseUrl()

        startKoin {
            printLogger()
            modules(module {
                single { AirportSteps() }
                single { BillingSteps() }
                single { settings }
            })
        }

        return super.beforeAllTestClasses(globalContext)
    }

    override suspend fun afterAllTestClasses(globalContext: CoroutineContext) {
        airlineServer.stop()
        super.afterAllTestClasses(globalContext)
    }
}