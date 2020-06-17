package ru.fix.kbdd.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.restassured.RestAssured.given
import io.restassured.config.DecoderConfig
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.response.Response
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import ru.fix.corounit.allure.AllureStep
import ru.fix.kbdd.asserts.AlluredKPath
import ru.fix.kbdd.asserts.Checkable
import ru.fix.kbdd.asserts.Explorable
import ru.fix.kbdd.asserts.KPath
import ru.fix.kbdd.json.Json
import ru.fix.kbdd.json.json
import ru.fix.kbdd.map.MapDsl
import java.io.InputStream
import java.util.concurrent.Executors

private val log = KotlinLogging.logger { }

object Rest {
    private val defaultMapper = jacksonObjectMapper()
    private val doNotSendNullsMapper = defaultMapper.copy()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    private fun selectMapper(sendNulls: Boolean = true) =
            if (sendNulls) {
                defaultMapper
            } else {
                doNotSendNullsMapper
            }


    private val lastResponse = ThreadLocal<Response>()

    var threadPoolSize = 10

    private val dispatcher by lazy {
        Executors.newFixedThreadPool(10).asCoroutineDispatcher() +
                CoroutineExceptionHandler { _, thr -> log.error(thr) {} }
    }


    /**
     * Creates request specification with default encodings, logging filters and Allure integration
     */
    suspend fun request(request: RequestDsl.() -> Unit): Unit {

        val config = RestAssuredConfig.config()
                .encoderConfig(
                        EncoderConfig.encoderConfig().defaultContentCharset(Charsets.UTF_8)
                                .defaultCharsetForContentType(Charsets.UTF_8, ContentType.JSON)
                )
                .decoderConfig(
                        DecoderConfig.decoderConfig().defaultContentCharset(Charsets.UTF_8)
                                .defaultCharsetForContentType(Charsets.UTF_8, ContentType.JSON)
                )

        val dsl = RequestDsl().apply(request)

        val allureStep = AllureStep.fromCurrentCoroutineContext()

        val spec = given()
                .filter(HttpAllureAttachmentFilter(allureStep))
                .run {
                    when {
                        dsl.formParams != null -> contentType(ContentType.URLENC)
                        dsl.bodyJsonDsl != null -> contentType(ContentType.JSON)
                        dsl.bodyString != null -> contentType(ContentType.JSON)
                        dsl.bodyXml != null -> contentType(ContentType.XML)
                        else -> this
                    }
                }
                .config(config)
                .run {
                    dsl.headers?.let { headers(it) } ?: this
                }
                .run {
                    dsl.baseUrl?.let { baseUri(it) } ?: this
                }
                .run {
                    when {
                        dsl.bodyJsonDsl != null -> {
                            val selectedMapper = selectMapper(dsl.bodyJsonSendNulls)
                            val objectNode = selectedMapper.json(dsl.bodyJsonDsl!!)
                            if (!dsl.bodyJsonSendNulls) {
                                removeNullFiledsInObjectNodes(listOf(objectNode))
                            }
                            val content = selectedMapper.writeValueAsString(objectNode)
                            body(content)
                        }

                        dsl.bodyString != null ->
                            body(dsl.bodyString!!)

                        dsl.bodyXml != null ->
                            body(dsl.bodyXml!!)

                        else -> this
                    }
                }
                .run {
                    dsl.formParams?.let { formParams(it) } ?: this
                }
                .run {
                    dsl.queryParams?.let { queryParams(it) } ?: this
                }
                .run {
                    dsl.filename?.let { name ->
                        dsl.fileContent?.let { content ->
                            multiPart("file", name, content)
                        }
                    } ?: this
                }


        val response = withContext(dispatcher) {
            try {
                when {
                    dsl.post != null -> spec.post(dsl.post)
                    dsl.get != null -> spec.get(dsl.get)
                    dsl.delete != null -> spec.delete(dsl.delete)
                    dsl.put != null -> spec.put(dsl.put)
                    else -> throw IllegalArgumentException(
                            "Neither post, get, delete or put method was declared in request")
                }
            } catch (exc: Exception) {
                throw RuntimeException("Failed to execute request with" +
                        " baseUrl: ${dsl.baseUrl}" +
                        ", path: ${dsl.post ?: dsl.get ?: dsl.delete ?: dsl.put}", exc)
            }
        }

        lastResponse.set(response)
    }

    private tailrec fun removeNullFiledsInObjectNodes(objectNodes: List<ObjectNode>) {
        if (objectNodes.isEmpty()) return

        val childContainers = mutableListOf<ObjectNode>()
        for (node in objectNodes) {
            for (name in node.fieldNames().asSequence().toList()) {
                val child = node[name]
                if (child.isNull) {
                    node.remove(name)
                } else {
                    if (child is ObjectNode) {
                        childContainers.add(child)
                    }
                }
            }
        }
        removeNullFiledsInObjectNodes(childContainers)
    }

    class RequestDsl {
        internal var bodyJsonSendNulls: Boolean = true
        internal var baseUrl: String? = null
        internal var bodyJsonDsl: (Json.() -> Unit)? = null
        internal var bodyString: String? = null
        internal var bodyXml: String? = null
        internal var post: String? = null
        internal var get: String? = null
        internal var delete: String? = null
        internal var put: String? = null
        internal var formParams: MutableMap<String, String>? = null
        internal var queryParams: MutableMap<String, String>? = null
        internal var headers: MutableMap<String, String>? = null
        internal var filename: String? = null
        internal var fileContent: InputStream? = null

        fun baseUri(baseUrl: String) {
            this.baseUrl = baseUrl
        }

        fun body(sendNulls: Boolean = true, body: Json.() -> Unit) {
            this.bodyJsonSendNulls = sendNulls
            this.bodyJsonDsl = body
        }

        fun body(body: String) {
            this.bodyString = body
        }


        fun body(pojo: Any, sendNulls: Boolean = true) {
            this.bodyString = selectMapper(sendNulls).writeValueAsString(pojo)
        }

        fun bodyXml(xml: String) {
            this.bodyXml = xml
        }

        fun post(path: String) {
            this.post = path
        }

        fun get(path: String) {
            this.get = path
        }

        fun delete(path: String) {
            this.delete = path
        }

        fun put(path: String) {
            this.put = path
        }

        private fun addFormParams(formParams: Map<String, String>) {
            this.formParams = (this.formParams ?: mutableMapOf()).apply { putAll(formParams) }
        }

        fun formParams(formParams: MapDsl<String>.() -> Unit) {
            addFormParams(MapDsl.map(formParams))
        }

        fun formParams(formParams: Map<String, String>) {
            addFormParams(formParams)
        }

        private fun addQueryParams(queryParams: Map<String, String>) {
            this.queryParams = (this.queryParams ?: mutableMapOf()).apply { putAll(queryParams) }
        }

        fun queryParams(queryParams: MapDsl<String>.() -> Unit) {
            addQueryParams(MapDsl.map(queryParams))
        }

        fun queryParams(queryParams: Map<String, String>) {
            addQueryParams(queryParams)
        }

        private fun addHeaders(headers: Map<String, String>) {
            this.headers = (this.headers ?: mutableMapOf()).apply { putAll(headers) }
        }

        fun headers(headers: MapDsl<String>.() -> Unit) {
            addHeaders(MapDsl.map(headers))
        }

        fun headers(headers: Map<String, String>) {
            addHeaders(headers)
        }

        fun file(filename: String, fileContent: InputStream) {
            this.filename = filename
            this.fileContent = fileContent
        }
    }

    /**
     * Builds json object using dsl
     */
    fun json(json: Json.() -> Unit): ObjectNode = defaultMapper.json(json)

    private suspend fun rawResponse() = lastResponse.get() ?: throw IllegalStateException("Previous response not found")

    /**
     * Returns last request
     */
    suspend fun statusCode(): Checkable {
        val response = rawResponse()
        return AlluredKPath(
                parentStep = AllureStep.fromCurrentCoroutineContext(),
                node = response.statusCode,
                mode = KPath.Mode.IMMEDIATE_ASSERT,
                path = "statusCode()"
        )
    }

    suspend fun statusLine(): Checkable {
        val response = rawResponse()
        return AlluredKPath(
                parentStep = AllureStep.fromCurrentCoroutineContext(),
                node = response.statusLine,
                mode = KPath.Mode.IMMEDIATE_ASSERT,
                path = "statusLine()"
        )
    }

    suspend fun bodyString(): Checkable {
        val response = rawResponse()
        return AlluredKPath(
                parentStep = AllureStep.fromCurrentCoroutineContext(),
                node = response.body().asString(),
                mode = KPath.Mode.IMMEDIATE_ASSERT,
                path = "bodyString()"
        )
    }

    suspend fun bodyJson(): Explorable {
        val response = rawResponse()
        return AlluredKPath(
                parentStep = AllureStep.fromCurrentCoroutineContext(),
                node = response.jsonPath().get<Any?>()!!,
                mode = KPath.Mode.IMMEDIATE_ASSERT,
                path = "bodyJson()"
        )
    }

    suspend fun bodyXml(): Explorable {
        val response = rawResponse()
        return AlluredKPath(
                parentStep = AllureStep.fromCurrentCoroutineContext(),
                node = response.xmlPath(),
                mode = KPath.Mode.IMMEDIATE_ASSERT,
                path = "bodyXml()"
        )
    }
}
