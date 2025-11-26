package cu.csca5028.alme9155.collector

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*

import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.Serializable

import java.util.TimeZone
import io.ktor.server.application.*

import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory  
import cu.csca5028.alme9155.logging.LogLevel
import cu.csca5028.alme9155.database.*
import cu.csca5028.alme9155.api.*
import cu.csca5028.alme9155.messaging.*

private val logger = BasicJSONLoggerFactory.getLogger("DataCollectorServer")

@Serializable
data class CollectionResult(
    val fetchedCount: Int,
    val upsertedCount: Int
)

@Serializable
data class PublishResult(
    val dbCount: Int,
    val msgCount: Int
)

fun Application.collectorModule() {
    val port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt()
        ?: System.getenv("PORT")?.toInt()
        ?: 8080

    install(ContentNegotiation) {
        json()
    }
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
                logger.info("Fetched $apiCount records from External API.")

                val reviews: List<RawMovieReview> = ApiDataCollector.getFetchedData()
                dbCount = MongoDBAdapter.upsertMoviesReviews(reviews)
                logger.info("Upserted $dbCount records to NoSQL database.")
            } catch (ex: Exception) {
                logger.error("Exception found during data collection", ex)
            }
            logger.info("POST /collect fetched $dbCount records to NoSQL data store.")
            call.respond(CollectionResult(fetchedCount = apiCount, upsertedCount = dbCount))
        }
        post("/publish") {
            logger.info("post /publish called.")

            var dbCount = 0
            var msgCount = 0
            val stats: PublishStats = try {
                ReviewsDataHandler.publishAllRawReviews()
            } catch (ex: Exception) {
                logger.error("Exception while publishing jobs to RabbitMQ", ex)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Error during /publish: ${ex.message}"
                )
                return@post
            }
            logger.info("POST /publish completed: totalRawReviews=${stats.totalRawReviews}, published=${stats.publishedCount}")
            call.respond(PublishResult(
                    dbCount = stats.totalRawReviews,
                    msgCount = stats.publishedCount
                )
            )
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