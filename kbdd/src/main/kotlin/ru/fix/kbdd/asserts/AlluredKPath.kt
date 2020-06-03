package ru.fix.kbdd.asserts

import ru.fix.corounit.allure.AllureStep

class AlluredKPath(
        private val parentStep: AllureStep,
        private val node: Any?,
        private val path: String = "",
        private val mode: Mode = Mode.IMMEDIATE_ASSERT,
        private val assertor: KPathAssertor = object : KPathAssertor {
            override fun assert(expressionPrint: String, actualValue: Any?, result: Boolean) {
                parentStep.step(expressionPrint, result)
                DefaultAssertor.assert(expressionPrint, actualValue, result)
            }
        }
) : KPath(node, path, mode, assertor)

