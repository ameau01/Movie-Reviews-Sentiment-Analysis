package cu.csca5028.alme9155.logging

import java.util.ServiceLoader

/**
 * Service provider implementation for Java's ServiceLoader.
 */
class BasicJSONServiceProvider : LoggerFactory {
    override fun getLogger(name: String): BasicJSONLogger = BasicJSONLoggerFactory.getLogger(name)
}

/**
 * Interface for logger factories (extendable for other implementations).
 */
interface LoggerFactory {
    fun getLogger(name: String): BasicJSONLogger
}

/**
 * Convenience extension for ServiceLoader.
 */
fun loadLoggerFactories(): List<LoggerFactory> = ServiceLoader.load(LoggerFactory::class.java).toList()