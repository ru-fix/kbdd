package ru.fix.kbdd.asserts

interface KPathAssertor {
    fun assert(expressionPrint: String, actualValue: Any?, result: Boolean)
}

object DefaultAssertor : KPathAssertor {
    override fun assert(expressionPrint: String, actualValue: Any?, result: Boolean) {
        val explanation = "$expressionPrint, actual:\n$actualValue"
        if (!result) {
            throw AssertionError(explanation)
        }
    }
}


/**
 * @param node either Map or List or Numeric or String
 */
open class KPath(private val node: Any?,
                 private val path: String = "",
                 private val mode: Mode = Mode.IMMEDIATE_ASSERT,
                 private val assertor: KPathAssertor = DefaultAssertor) : Explorable {

    enum class Mode { LAZY_EVALUATE, IMMEDIATE_ASSERT }

    private fun context() = object : NavigationContext {
        override val path: String
            get() = this@KPath.path
        override val node: Any?
            get() = this@KPath.node

        override fun evaluatePredicate(item: Any?, predicate: (Explorable) -> Expression): Boolean {
            val expression = predicate(KPath(
                    node = item,
                    path = "it",
                    mode = Mode.LAZY_EVALUATE,
                    assertor = assertor))
            return expression.evaluate()
        }
    }

    override fun navigate(navigation: NavigationContext.() -> Navigation): Explorable {
        val navigation = context().navigation()
        return KPath(
                node = navigation.node(),
                path = navigation.path(),
                mode = mode,
                assertor = assertor)
    }

    override fun <T> export(exporter: NavigationContext.() -> T): T {
        return context().exporter()
    }

    override fun express(expression: (Source) -> Expression): Expression {
        val source = object : Source {
            override fun print(): String = path
            override fun evaluate(): Any? = node
        }
        val expression = expression(source)
        when (mode) {
            Mode.IMMEDIATE_ASSERT -> {
                val result = expression.evaluate()
                assertor.assert(expression.print(), source.evaluate(), result)
                return expression
            }
            Mode.LAZY_EVALUATE -> {
                return expression
            }
        }
    }
}