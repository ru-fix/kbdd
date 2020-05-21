package ru.fix.kbdd.asserts

import java.math.BigDecimal
import java.math.BigInteger

interface Expression {
    fun print(): String
    fun evaluate(): Boolean

    infix fun and(other: Expression): Expression = AndExpression(this, other)
    infix fun or(other: Expression): Expression = OrExpression(this, other)
    infix fun not(other: Expression): Expression = NotExpression(this)
}

class AndExpression(
        private val left: Expression,
        private val right: Expression) : Expression {

    override fun print(): String = "(${left.print()}) and (${right.print()})"
    override fun evaluate(): Boolean = left.evaluate() && right.evaluate()
}

class OrExpression(
        private val left: Expression,
        private val right: Expression) : Expression {

    override fun print(): String = "(${left.print()}) or (${right.print()})"
    override fun evaluate(): Boolean = left.evaluate() || right.evaluate()
}

class NotExpression(private val exp: Expression) : Expression {
    override fun print(): String = "not (${exp.print()})"
    override fun evaluate(): Boolean = !exp.evaluate()
}

interface Source {
    fun print(): String
    fun evaluate(): Any?
}

interface Checkable {
    fun express(expression: (Source) -> Expression): Expression
}


fun Checkable.isEquals(other: Any?) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} == ${
        if (other != null && other is String) "\"$other\"" else "$other"
        }"

        override fun evaluate(): Boolean = checkValuesEqualityWithStringAutoCast(source.evaluate(), other)
    }
}

fun Checkable.isNotEquals(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} != $other"
        override fun evaluate(): Boolean = source.evaluate() != other
    }
}

fun Checkable.isNull() = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} == null"
        override fun evaluate(): Boolean = source.evaluate() == null
    }
}

fun Checkable.isNotNull() = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} != null"
        override fun evaluate(): Boolean = source.evaluate() != null
    }
}

private fun compareValuesWithStringAutoCast(first: Any, second: Any): Int {
    val firstString = first.toString()
    when (second) {
        is Boolean -> return firstString.toBoolean().compareTo(second)
        is Byte -> return firstString.toByte().compareTo(second)
        is Short -> return firstString.toShort().compareTo(second)
        is Int -> return firstString.toInt().compareTo(second)
        is Float -> return firstString.toFloat().compareTo(second)
        is Double -> return firstString.toDouble().compareTo(second)
        is Long -> return firstString.toLong().compareTo(second)
        is BigDecimal -> return firstString.toBigDecimal().compareTo(second)
        is BigInteger -> return firstString.toBigInteger().compareTo(second)
    }
    return (first as Comparable<Any>).compareTo(second)
}

private fun checkValuesEqualityWithStringAutoCast(first: Any?, second: Any?): Boolean {
    if(first == null && second != null) return false
    if(first != null && second == null) return false
    if (first == null && second == null) return true
    val firstString = first.toString()
    when (second) {
        is Boolean -> return firstString.toBoolean() == second
        is Byte -> return firstString.toByte() == second
        is Short -> return firstString.toShort() == second
        is Int -> return firstString.toInt() == second
        is Float -> return firstString.toFloat() == second
        is Double -> return firstString.toDouble() == second
        is Long -> return firstString.toLong() == second
        is BigDecimal -> return firstString.toBigDecimal() == second
        is BigInteger -> return firstString.toBigInteger() == second
    }
    return first == second
}

fun Checkable.isLessThan(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} < $other"
        override fun evaluate(): Boolean = compareValuesWithStringAutoCast(source.evaluate()!!, other) < 0
    }
}

fun Checkable.isLessThanOrEqual(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} <= $other"
        override fun evaluate(): Boolean = compareValuesWithStringAutoCast(source.evaluate()!!, other) <= 0
    }
}

fun Checkable.isGreaterThan(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} > $other"
        override fun evaluate(): Boolean = compareValuesWithStringAutoCast(source.evaluate()!!, other) > 0
    }
}

fun Checkable.isGreaterThanOrEqual(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} >= $other"
        override fun evaluate(): Boolean = compareValuesWithStringAutoCast(source.evaluate()!!, other) >= 0
    }
}


fun Checkable.isContains(text: String) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()}.isContains($text)"
        override fun evaluate(): Boolean = source.evaluate().toString().contains(text)
    }
}

fun Checkable.isMatches(regex: String) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()}.isMatches(\"$regex\")"
        override fun evaluate(): Boolean = source.evaluate().toString().matches(regex.toRegex())
    }
}

fun Checkable.shouldBeIn(vararg values: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} in ${values.toList()}"
        override fun evaluate(): Boolean = source.evaluate() in values
    }
}

fun Checkable.shouldBeIn(values: Collection<Any>) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} in $values"
        override fun evaluate(): Boolean = source.evaluate() in values
    }
}

fun Checkable.shouldNotBeIn(vararg values: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} not in ${values.toList()}"
        override fun evaluate(): Boolean = source.evaluate() !in values
    }
}

fun Checkable.shouldNotBeIn(values: Collection<Any>) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} not in $values"
        override fun evaluate(): Boolean = source.evaluate() !in values
    }
}