package test.cu.csca5028.alme9155.logging

import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BasicJSONServiceProviderTest {

    private val originalOut = System.out
    private val output = ByteArrayOutputStream()

    @BeforeEach
    fun setUp() {
        output.reset()
        System.setOut(PrintStream(output, true, Charsets.UTF_8))
    }

    @AfterEach
    fun tearDown() {
        System.setOut(originalOut)
    }

    private fun outputLines(): List<String> =
        output.toString(Charsets.UTF_8)
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

    @Test
    fun logJSONTest() {
        val logger = BasicJSONLoggerFactory.getLogger("MyService")
        logger.info("Test message")

        val line = outputLines().single()
        assertTrue(line.startsWith("{") && line.endsWith("}"))
        assertContains(line, """"level":"INFO"""")
        assertContains(line, """"logger":"MyService"""")
        assertContains(line, """"message":"Test message"""")
    }

    @Test
    fun logLevelTest() {
        val logger = BasicJSONLoggerFactory.getLogger("Levels")

        logger.debug("debug")
        logger.info("info")
        logger.warn("warn")
        logger.error("error")

        val lines = outputLines()
        assertEquals(4, lines.size, "Exactly 4 log lines expected")
    }

    @Test
    fun logExceptionTest() {
        val logger = BasicJSONLoggerFactory.getLogger("Error")
        val ex = RuntimeException("Boom")
        logger.error("Failed", ex)

        val line = outputLines().single()
        assertContains(line, """"message":"Failed"""")
        assertContains(line, """"exception":""")
        assertContains(line, "java.lang.RuntimeException: Boom")
    }
}