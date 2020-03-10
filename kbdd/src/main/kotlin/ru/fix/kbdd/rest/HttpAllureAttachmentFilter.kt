package ru.fix.kbdd.rest

import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.filter.log.LogDetail
import io.restassured.filter.log.UrlDecoder
import io.restassured.internal.print.RequestPrinter
import io.restassured.internal.print.ResponsePrinter
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import mu.KotlinLogging
import ru.fix.corounit.allure.AllureStep
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset

class HttpAllureAttachmentFilter(private val allureStep: AllureStep) : Filter {
    private companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun filter(
            requestSpec: FilterableRequestSpecification,
            responseSpec: FilterableResponseSpecification,
            ctx: FilterContext
    ): Response {

        val uri = UrlDecoder.urlDecode(
                requestSpec.uri,
                Charset.forName(requestSpec.config.encoderConfig.defaultQueryParameterCharset()),
                true)

        val requestOutput = withStringPrintStream { printStream ->
            RequestPrinter.print(
                    requestSpec,
                    requestSpec.method,
                    uri,
                    LogDetail.ALL,
                    emptySet(),
                    printStream,
                    true)
        }

        allureStep.attachment("request $uri", requestOutput)
        log.debug(requestOutput)

        val response = ctx.next(requestSpec, responseSpec)

        val responseOutput = withStringPrintStream { printStream ->
            ResponsePrinter.print(
                    response,
                    response,
                    printStream,
                    LogDetail.ALL,
                    true,
                    emptySet())
        }

        allureStep.attachment("response $uri", responseOutput)
        log.debug(responseOutput)

        return response
    }

    private fun withStringPrintStream(block: (PrintStream) -> Unit): String {
        val buffer = ByteArrayOutputStream()
        val printStream = PrintStream(buffer, false, Charsets.UTF_8)
        block(printStream)
        return buffer.toString(Charsets.UTF_8)
    }
}