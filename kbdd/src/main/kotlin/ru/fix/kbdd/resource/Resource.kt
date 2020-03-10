package ru.fix.kbdd.resource

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.reflect.KClass


object Resource {
    private val mapper = ObjectMapper().registerKotlinModule()

    fun loadResourceWithPlaceholders(location: String): String {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(location)
        require(stream != null) { "Can not load resource from: $location" }

        val content = stream.use {
            it.reader().readText()
        }
        return evaluatePlaceholders(content)
    }

    inline fun <reified T> loadJsonWithPlaceholders(location: String): T = loadJsonWithPlaceholders(location, T::class)

    fun <T> loadJsonWithPlaceholders(location: String, type: KClass<*>): T {
        val json = loadResourceWithPlaceholders(location)
        return mapper.readValue(json, type.java) as T
    }

    fun evaluatePlaceholders(content: String): String {
        var output = content
        getPlaceholdersData().forEach { (name, value) ->
            output = output.replace("\${$name}", value)
        }
        return output
    }

    private fun getPlaceholdersData(): Map<String, String> {
        val data = HashMap<String, String>()

        System.getenv().forEach { (k, v) -> data.put(k, v) }

        val systemProperties = System.getProperties()
        systemProperties.stringPropertyNames()
                .map { it to systemProperties.getProperty(it) }
                .filter { it.second != null }
                .forEach { data.put(it.first, it.second) }

        return data
    }
}