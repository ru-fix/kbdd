package ru.fix.kbdd.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeCreator
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * JSON DSL
 * @see Json
 */
fun JsonNodeCreator.json(json: Json.() -> Unit): ObjectNode {
    val context = Json(this)
    json.invoke(context)
    return context.jsonNode
}

/**
 * JSON DSL
 * @see Json
 */
fun ObjectMapper.json(json: Json.() -> Unit): ObjectNode {
    val context = Json(this.nodeFactory)
    json.invoke(context)
    return context.jsonNode
}

fun json(json: Json.() -> Unit): ObjectNode {
    val objectMapper = ObjectMapper().registerKotlinModule()
    val context = Json(objectMapper.nodeFactory)
    json.invoke(context)
    return context.jsonNode
}


/**
 * DSL for building json
 * ```
 * "data" {
 *     "array" % array(1, 2, 3)
 *     "singleElementArray" % array(1)
 *     "emptyArray" % array()
 *     "complexObject" {
 *         "string" % "43"
 *         "numeric" % 43
 *     }
 *     "emptyComplex" {}
 *     "arrayOfDifferentThings" % array("string", 27, { "name" % "first" }, { "name" % "second" })
 *     "numeric" % 12
 *     """escaped"String""" % """m"y"""
 *     "nullable" % null
 *     "complexInArray" % array(
 *         {
 *            "name" % "one"
 *            "value" % 1
 *         },
 *         {
 *            "name" % "two"
 *            "value" % 2
 *         })
 *     "singleComplexInArray" % array({ "number" % 12 })
 *     "singleEmptyComplexInArray" % array({})
 * }
 * ```
 */
class Json(private val jsonNodeCreator: JsonNodeCreator) {
    var jsonNode: ObjectNode = jsonNodeCreator.objectNode()

    fun array(vararg array: Any?): Array<out Any?> = array

    operator fun String.invoke(json: () -> Unit) {
        val prev = jsonNode
        jsonNode = jsonNodeCreator.objectNode()
        json.invoke()
        prev.set<JsonNode>(this, jsonNode)
        jsonNode = prev
    }

    operator fun String.rem(value: Json.() -> Unit) {
        val context = Json(jsonNodeCreator)
        value.invoke(context)
        jsonNode.set<JsonNode>(this, context.jsonNode)
    }

    operator fun String.rem(value: () -> Any?) {
        val prev = jsonNode
        jsonNode = jsonNodeCreator.objectNode()
        value.invoke()
        prev.set<JsonNode>(this, jsonNode)
        jsonNode = prev
    }

    operator fun String.rem(value: Any?) {
        when (value) {
            null -> jsonNode.putNull(this)
            is Boolean -> jsonNode.put(this, value)
            is Short -> jsonNode.put(this, value)
            is Int -> jsonNode.put(this, value)
            is Long -> jsonNode.put(this, value)
            is Float -> jsonNode.put(this, value)
            is Double -> jsonNode.put(this, value)
            is String -> jsonNode.put(this, value)
            is LocalDate -> jsonNode.put(this, DateTimeFormatter.ISO_LOCAL_DATE.format(value))
            is LocalDateTime -> jsonNode.put(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value))
            is OffsetDateTime -> jsonNode.put(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value))
            is JsonNode -> jsonNode.set(this, value)
            is Array<*> -> putArray(this, value.iterator())
            is Iterable<*> -> putArray(this, value.iterator())
            else -> jsonNode.set(this, jsonNodeCreator.pojoNode(value))
        }
    }

    private fun putArrayContent(arrayNode: ArrayNode, data: Iterator<Any?>) {
        for (value in data) {
            when (value) {
                null -> arrayNode.addNull()
                is Boolean -> arrayNode.add(value)
                is Short -> arrayNode.add(value.toInt())
                is Int -> arrayNode.add(value)
                is Long -> arrayNode.add(value)
                is Float -> arrayNode.add(value)
                is Double -> arrayNode.add(value)
                is String -> arrayNode.add(value)
                is JsonNode -> arrayNode.add(value)
                is LocalDate -> arrayNode.add(DateTimeFormatter.ISO_LOCAL_DATE.format(value))
                is LocalDateTime -> arrayNode.add(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value))
                is OffsetDateTime -> arrayNode.add(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value))

                is Array<*> -> arrayNode.addArray().apply {
                    putArrayContent(this, value.iterator())
                }
                is Iterable<*> -> arrayNode.addArray().apply {
                    putArrayContent(this, value.iterator())
                }
                is () -> Any? -> {
                    val prev = jsonNode
                    jsonNode = jsonNodeCreator.objectNode()
                    value.invoke()
                    arrayNode.add(jsonNode)
                    jsonNode = prev
                }
                else -> arrayNode.add(jsonNodeCreator.pojoNode(value))
            }
        }
    }

    private fun putArray(name: String, data: Iterator<Any?>) {
        val jsonArray = jsonNode.putArray(name)
        putArrayContent(jsonArray, data)
    }

}