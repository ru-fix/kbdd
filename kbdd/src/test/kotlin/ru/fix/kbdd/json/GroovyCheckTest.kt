package ru.fix.kbdd.json

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.qameta.allure.model.Status
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import ru.fix.corounit.allure.AllureStep
import ru.fix.kbdd.rest.GroovyCheck

private val log = KotlinLogging.logger { }

class GroovyCheckTest {
    @Test
    fun check() {
        val step = AllureStep()

        try {
            runBlocking(step) {
                val data = mutableMapOf<String, Any?>(
                        "complex" to mapOf(
                                "stringInComplex" to "yellow",
                                "intInComplex" to 17
                        ),
                        "array" to listOf(
                                mapOf(
                                        "name" to "one",
                                        "value" to 1
                                ),
                                mapOf(
                                        "name" to "two",
                                        "value" to 2
                                ))
                )

                GroovyCheck.assert(data, """array.findAll{it.value > 0}.name.contains("one")""")


                GroovyCheck.assert(data, """array.findAll{it.name == "two"}.value == 1""")
            }
            fail("assert should throw an exception")

        } catch (err: AssertionError) {

        }

        val children = step.children.toList()
        children[0].step.apply {
            name.shouldContain("it.value > 0")
            status.shouldBe(Status.PASSED)
        }
        children[1].step.apply {
            name.shouldContain("""it.name == "two"""")
            status.shouldBe(Status.FAILED)
        }
    }
}