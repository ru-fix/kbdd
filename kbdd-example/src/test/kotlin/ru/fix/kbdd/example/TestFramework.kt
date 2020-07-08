package ru.fix.kbdd.example

import org.apache.logging.log4j.kotlin.logger
import ru.fix.corounit.allure.AllureStep
import ru.fix.corounit.allure.invoke
import java.nio.file.Files
import java.nio.file.Paths

object TestFramework {

    private val logger = logger("TestFramework")

    suspend fun makeCodeSnippet() {
        try {
            val stackFrame = Thread.currentThread().stackTrace[2]
            val sourceFile = Files.walk(Paths.get(""))
                    .filter { it.fileName.toString() == stackFrame.fileName }
                    .findFirst()
                    .get()
            val sourceLines = Files.readAllLines(sourceFile)

            var start = stackFrame.lineNumber
            while (start > 0 && !sourceLines[start].contains("@Test"))
                start--

            var end = stackFrame.lineNumber
            while (end < sourceLines.size - 1 && !sourceLines[end].contains("@Test")) {
                end++
            }

            val snapshot = sourceLines.foldIndexed("") { index, acc, line ->
                if (index in start..end && !line.contains(TestFramework::makeCodeSnippet.name))
                    acc + line + "\n"
                else
                    acc
            }

            "Code snippet"{
                AllureStep.attachment("code-snippet", snapshot)
            }
        } catch (exc: Exception) {
            logger.error(exc) { "Code snapshot failed during test execution" }
        }
    }
}