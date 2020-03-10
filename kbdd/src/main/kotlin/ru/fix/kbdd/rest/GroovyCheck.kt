package ru.fix.kbdd.rest

import groovy.lang.Binding
import groovy.lang.GroovyShell
import ru.fix.corounit.allure.AllureStep

object GroovyCheck {
    suspend fun assert(params: Map<String, Any?>, expression: String) {
        AllureStep.fromCurrentCoroutineContext().step(expression) {

            val script = "assert ${expression.trim()}"

            val binding = Binding(params)
            val shell = GroovyShell(binding);

            try {
                shell.evaluate(script)
            } finally {
                shell.resetLoadedClasses()
            }
        }
    }
}