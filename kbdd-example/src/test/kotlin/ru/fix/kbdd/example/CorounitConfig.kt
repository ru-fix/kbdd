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

    private val mockServer = MockServer()

    init {
        Rest.threadPoolSize = 10
    }

    override suspend fun beforeAllTestClasses(globalContext: CoroutineContext): CoroutineContext {
        mockServer.start()

        val settings = Settings()
        settings.baseUri = mockServer.baseUrl()

        startKoin {
            printLogger()
            modules(module {
                single { createStepClassInstance<AirportSteps>() }
                single { createStepClassInstance<BillingSteps>() }
                single { settings }
                single { mockServer }
            })
        }

        return super.beforeAllTestClasses(globalContext)
    }

    override suspend fun afterAllTestClasses(globalContext: CoroutineContext) {
        mockServer.stop()
        super.afterAllTestClasses(globalContext)
    }
}