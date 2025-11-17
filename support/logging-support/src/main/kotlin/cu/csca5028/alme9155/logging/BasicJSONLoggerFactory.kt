package cu.csca5028.alme9155.logging

/**
 * Factory for creating BasicJSONLogger instances.
 */
object BasicJSONLoggerFactory {
    private val loggers = mutableMapOf<String, BasicJSONLogger>()

    @Synchronized
    fun getLogger(name: String): BasicJSONLogger {
        return loggers.getOrPut(name) { BasicJSONLogger(name) }
    }

    /**
     * Clear all loggers (for testing).
     */
    @Synchronized
    fun clear() {
        loggers.clear()
    }
}