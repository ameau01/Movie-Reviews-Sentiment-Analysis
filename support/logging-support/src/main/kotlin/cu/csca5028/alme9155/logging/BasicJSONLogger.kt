package cu.csca5028.alme9155.logging

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Thread-safe JSON logger for server components.
 */
class BasicJSONLogger(private val name: String) {
    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"))

    fun debug(msg: String) = log("DEBUG", msg)

    fun info(msg: String) = log("INFO", msg)

    fun warn(msg: String) = log("WARN", msg)

    fun error(msg: String, throwable: Throwable? = null) = log("ERROR", msg, throwable)

    private fun log(level: String, msg: String, throwable: Throwable? = null) {
        val timestamp = formatter.format(Instant.now())
        val threadId = Thread.currentThread().id
        val logEntry = LogEntry(
            timestamp = timestamp,
            level = level,
            logger = name,
            thread = threadId,
            message = msg,
            exception = throwable?.let { serializeException(it) }
        )
        val jsonString = json.encodeToString(logEntry)
        synchronized(System.out) {
            System.out.println(jsonString)
        }
    }

    private fun serializeException(t: Throwable): String {
        val sw = StringWriter()
        t.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}

@Serializable
data class LogEntry(
    val timestamp: String,
    val level: String,
    val logger: String,
    val thread: Long,
    val message: String,
    val exception: String? = null
)