package cu.csca5028.alme9155.collector

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*

import java.util.TimeZone
import io.ktor.server.application.*

import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory  
import cu.csca5028.alme9155.logging.LogLevel

import cu.csca5028.alme9155.database.*
import cu.csca5028.alme9155.api.*

private val logger = BasicJSONLoggerFactory.getLogger("DataCollectorServer")

fun Application.collectorModule() {
    val port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt()
        ?: System.getenv("PORT")?.toInt()
        ?: 8080

    routing {
        get("/") {
            logger.info("get / called.")
            val usage = """
                AI-Powered Movie Sentiment Rating System
                ------------------------------------------
                Data Collector API
                Running on: http://localhost:$port
                
                Usage:
                - POST /collect
                    
                Example (curl):
                curl -X POST http://localhost:$port/collect \
                    
                Health check:
                curl http://localhost:$port/health

            """.trimIndent()

            call.respondText(usage, ContentType.Text.Plain)
        }
        get("/health") {
            logger.info("get /health called.")
            call.respondText("OK", ContentType.Text.Plain)
        }
        post("/collect") {
            logger.info("post /collect called.")

            var dbCount = 0
            var apiCount = 0
            try {
                apiCount = ApiDataCollector.fetchDataFromAPI()
                logger.info("Fetched $apiCount records from External API")

                val reviews: List<RawMovieReview> = ApiDataCollector.getFetchedData()
                dbCount = MongoDBAdapter.upsertMoviesReviews(reviews)
            } catch (ex: Exception) {
                logger.error("Exception found during data collection", ex)
            }
            logger.info("POST /collect fetched $dbCount records to NoSQL data store.")
            call.respondText("OK", ContentType.Text.Plain)
        }
    }
}

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val level = when (System.getenv("LOG_LEVEL")?.uppercase()) {
        "DEBUG" -> LogLevel.DEBUG
        "WARN"  -> LogLevel.WARN
        "ERROR" -> LogLevel.ERROR
        else    -> LogLevel.INFO
    }
    BasicJSONLoggerFactory.setLevel(level)

    val port = System.getenv("PORT")?.toInt() ?: 8080
    val logger = BasicJSONLoggerFactory.getLogger("DataCollectorServer")

    logger.info("Starting Data Collector API on port $port")
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        collectorModule()
    }.start(wait = true)
}