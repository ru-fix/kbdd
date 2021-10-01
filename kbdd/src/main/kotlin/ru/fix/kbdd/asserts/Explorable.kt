package ru.fix.kbdd.asserts

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.path.xml.XmlPath
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

internal class PathRecorder(val path: String) : Explorable {
    private fun context() = object : NavigationContext {
        override val path: String
            get() = this@PathRecorder.path
        override val node: Any
            get() = throw UnsupportedOperationException()
        override val objectMapper: ObjectMapper
            get() = throw UnsupportedOperationException()

        override fun evaluatePredicate(item: Any?, predicate: (Explorable) -> Expression): Boolean {
            throw UnsupportedOperationException()
        }
    }

    override fun navigate(navigation: NavigationContext.() -> Navigation): Explorable {
        val navigation = context().navigation()
        return PathRecorder(navigation.path())
    }

    override fun express(operation: (Source) -> Expression): Expression {
        val source = object : Source {
            override fun print(): String = path
            override fun evaluate(): Any? = throw UnsupportedOperationException()
        }
        return operation(source)
    }

    override fun <T> export(exporter: NavigationContext.() -> T): T = throw UnsupportedOperationException()
}

interface NavigationContext {
    val path: String
    val node: Any?
    val objectMapper: ObjectMapper

    fun evaluatePredicate(item: Any?, predicate: (Explorable) -> Expression): Boolean

    fun printPredicate(predicate: (Explorable) -> Expression): String {
        val pathRecorder = PathRecorder("it")
        val expression = predicate(pathRecorder)
        return expression.print()
    }

    fun requireNotNullNode(path: String): Any {
        return requireNotNull(node) {
            "Failed to access $path. Current value is null."
        }
    }

    fun requireNotNullMap(path: String): Map<String, Any?> {
        val node = requireNotNullNode(path)
        require(node is Map<*, *>) {
            "Failed to access $path. Current value is not a Map. Actual type: ${node.javaClass}"
        }
        return node as Map<String, Any?>
    }

    fun requireNotNullList(path: String): List<Any?> {
        val node = requireNotNullNode(path)
        require(node is List<*>) {
            "Failed to access $path. Current value is not a List. Actual type: ${node.javaClass}"
        }
        return node
    }
}


interface Navigation {
    fun path(): String
    fun node(): Any?
}

interface Explorable : Checkable {
    fun navigate(navigation: NavigationContext.() -> Navigation): Explorable
    fun <T> export(exporter: NavigationContext.() -> T): T
}

/**
 * Assert that current value can be represented as a `Map<String, Any?>`
 * and returns content as a `Map<String, Any?>`
 */
fun Explorable.asMap() = export {
    requireNotNullMap(path) as Map<String, Any?>
}

/**
 * Assert that current value can be represented as a `Map<String, Any?>`
 * and returns content as a `Map<String, Any?>`
 * Returns Null if the path is not exist
 */
fun Explorable.asMapOrNull() = export {
    if (node == null) {
        null
    } else {
        requireNotNullMap(path) as Map<String, Any?>
    }
}


/**
 * Assert that current value can be represented as a `List`
 * and returns content as a `List`
 */
fun <T : Any?> Explorable.asList(clazz: Class<T>) = export {
    val list = requireNotNullList(path)
    if (clazz.isPrimitive
        || List::class.java.isAssignableFrom(clazz)
        || Map::class.java.isAssignableFrom(clazz)
    ) {
        list as List<T>
    } else {
        list.map {
            objectMapper.convertValue(it, clazz)
        }
    }
}

/**
 * Assert that current value can be represented as a `List`
 * and returns content as a `List`
 * Returns NUll if the path is missing.
 */
fun <T : Any?> Explorable.asListOrNull(clazz: Class<T>) = export {
    if (node == null) {
        null
    } else {
        asList(clazz)
    }
}

inline fun <reified T : Any?> Explorable.asList() = asList(T::class.java)
inline fun <reified T : Any?> Explorable.asListOrNull() = asListOrNull(T::class.java)

fun Explorable.asListOfMaps() = export {
    requireNotNullList(path) as List<Map<String, Any?>>
}

fun Explorable.asListOfMapsOrNull() = export {
    if(node == null)
        null
    else
        asListOfMaps()
}

fun Explorable.asInt() = export {
    requireNotNullNode(path)
    when (val n = node) {
        is String -> n.toInt()
        else -> (n as Number).toInt()
    }
}

fun Explorable.asIntOrNull() = export {
    if (node == null) {
        null
    } else {
        asInt()
    }
}

fun Explorable.asLong() = export {
    requireNotNullNode(path)
    when (val n = node) {
        is String -> n.toLong()
        else -> (n as Number).toLong()
    }
}

fun Explorable.asLongOrNull() = export {
    if(node == null) null else asLong()
}

fun Explorable.asBoolean() = export {
    requireNotNullNode(path)
    when (val n = node) {
        is String -> n.toBoolean()
        else -> n as Boolean
    }
}

fun Explorable.asBooleanOrNull() = export {
    if(node == null) null else asBoolean()
}

fun Explorable.asString() = export {
    requireNotNullNode(path)
    node.toString()
}
fun Explorable.asStringOrNull() = export {
    if(node == null)
        null
    else
        asString()
}

fun <T> Explorable.asObject(clazz: Class<T>): T = export {
    val map = requireNotNullMap(path)
    this.objectMapper.convertValue(map, clazz)
}

fun <T> Explorable.asObjectOrNull(clazz: Class<T>): T? = export {
    if (node == null) {
        null
    } else {
        asObject(clazz)
    }
}

inline fun <reified T> Explorable.asObject(): T = asObject(T::class.java)
inline fun <reified T> Explorable.asObjectOrNull(): T? = asObjectOrNull(T::class.java)

/**
 * @return OffsetDateTime, parsing using ISO_ZONED_DATE_TIME formatter
 **/
fun Explorable.asOffsetDateTime() = this.asOffsetDateTime(DateTimeFormatter.ISO_ZONED_DATE_TIME)

/**
 * @return OffsetDateTime, parsing using ISO_ZONED_DATE_TIME formatter
 **/
fun Explorable.asOffsetDateTimeOrNull() = this.asOffsetDateTimeOrNull(DateTimeFormatter.ISO_ZONED_DATE_TIME)

fun Explorable.asOffsetDateTime(formatter: DateTimeFormatter) = export {
    requireNotNullNode(path)
    OffsetDateTime.parse(node.toString(), formatter)
}

fun Explorable.asOffsetDateTimeOrNull(formatter: DateTimeFormatter) = export {
    if(node == null) null else asOffsetDateTime(formatter)
}
/**
 * @return LocalDateTime, parsing using ISO_ZONED_DATE_TIME formatter
 **/
fun Explorable.asLocalDateTime() = this.asLocalDateTime(DateTimeFormatter.ISO_ZONED_DATE_TIME)

fun Explorable.asLocalDateTimeOrNull() = this.asLocalDateTimeOrNull(DateTimeFormatter.ISO_ZONED_DATE_TIME)

fun Explorable.asLocalDateTime(formatter: DateTimeFormatter) = export {
    requireNotNullNode(path)
    LocalDateTime.parse(node.toString(), formatter)
}

fun Explorable.asLocalDateTimeOrNull(formatter: DateTimeFormatter) = export {
    if(node == null) null else asLocalDateTime(formatter)
}

operator fun Explorable.get(index: Int) = navigate {
    object : Navigation {
        override fun path() = "$path[$index]"
        override fun node(): Any? {
            if(node == null){
                return null
            } else {
                val node = requireNotNullList(path())
                return node[index]
            }
        }
    }
}

operator fun Explorable.get(property: String) = navigate {
    object : Navigation {
        override fun path() = "$path[\"$property\"]"
        override fun node(): Any? {
            if(node == null) {
                return null
            }  else {
                val node = requireNotNullMap(path())
                return node[property]
            }
        }
    }
}

fun <T> Explorable.map(mapping: (Explorable) -> T): List<T> {
    return (0 until this.size().asInt()).map { index ->
        mapping(this[index])
    }
}

fun Explorable.xmlPath(xmlPath: String) = navigate {
    object : Navigation {
        override fun path() = "$path.xmlPath(\"$xmlPath\")"
        override fun node(): Any? {
            val node = requireNotNullNode(path())
            return (node as XmlPath).get<Any?>(xmlPath)
        }
    }
}

fun Explorable.size() = navigate {
    object : Navigation {
        override fun path() = "$path.size()"
        override fun node(): Any? {
            val node = requireNotNullList(path())
            return node.size
        }
    }
}

fun Explorable.single() = navigate {
    object : Navigation {
        override fun path() = "$path.single()"
        override fun node(): Any? {
            if(node == null){
                return null
            } else {
                val node = requireNotNullList(path())
                require(node.size == 1) {
                    "Failed to evaluatte $path(). Expected single element in the List. Actual: $node"
                }
                return node[0]
            }
        }
    }
}

fun Explorable.filter(predicate: (Explorable) -> Expression) = navigate {
    object : Navigation {
        override fun path() = "$path.filter{${printPredicate(predicate)}}"
        override fun node(): Any? {
            if(node == null){
                return null
            } else {
                val node = requireNotNullList(path())
                return if (node.isEmpty()) {
                    node
                } else {
                    node.filter { evaluatePredicate(it, predicate) }
                }
            }
        }
    }
}

fun Explorable.assert(predicate: (Explorable) -> Expression): Unit {

    export {
        val assertPath = "$path.assert{${printPredicate(predicate)}}"
        val result = evaluatePredicate(node, predicate)
        if (!result) {
            throw AssertionError("Assert: $assertPath failed. Node: $node")
        }
    }
}

fun Explorable.first(predicate: (Explorable) -> Expression) = navigate {
    object : Navigation {
        override fun path() = "$path.first{${printPredicate(predicate)}}"
        override fun node(): Any? {
            if(node == null) return null

            val node = requireNotNullList(path())
            require(node.isNotEmpty()) {
                "Failed to access ${path()}. Current List does not contain elements."
            }
            return node.first { evaluatePredicate(it, predicate) }
        }
    }
}

fun Explorable.single(predicate: (Explorable) -> Expression) = navigate {
    object : Navigation {
        override fun path() = "$path.single{${printPredicate(predicate)}}"
        override fun node(): Any? {
            if(node == null) return null

            val node = requireNotNullList(path())
            require(node.isNotEmpty()) {
                "Failed to access ${path()}. Current List does not contain elements."
            }

            val filteredList = node.filter { evaluatePredicate(it, predicate) }

            require(filteredList.size == 1) {
                "Failed to access ${path()}. Filtered list contains more than one element: ${filteredList}." +
                        " List before filtering is $node."
            }
            return filteredList.single()
        }
    }
}