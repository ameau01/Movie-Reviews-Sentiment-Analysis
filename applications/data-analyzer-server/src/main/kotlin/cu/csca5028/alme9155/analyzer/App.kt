package cu.csca5028.alme9155.analyzer

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.TimeZone
import io.ktor.server.application.*

import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory  
import cu.csca5028.alme9155.logging.LogLevel
import cu.csca5028.alme9155.sentiment.*
import cu.csca5028.alme9155.database.*
import cu.csca5028.alme9155.messaging.*

import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

private val logger = BasicJSONLoggerFactory.getLogger("DataAnalyzerServer")
private val appStartTimeMillis: Long = System.currentTimeMillis()

fun Application.analyzerModule() {
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
                Data Analyzer API
                Running on: http://localhost:$port

                Usage:
                - POST /analyze
                    * Content-Type: application/json
                    * Body: { 
                        "title": "your movie title here" 
                        "text": "your movie review here" 
                    }
                    
                Example (curl):
                curl -X POST http://localhost:$port/analyze \
                    -H "Content-Type: application/json" \
                    -d '{"title":"God Father","text":"Amazing acting and plot!"}' 
                    
                Health check:
                curl http://localhost:$port/health

            """.trimIndent()

            call.respondText(usage, ContentType.Text.Plain)
        }
        get("/health") {
            logger.info("get /health called.")
            call.respondText("OK", ContentType.Text.Plain)
        }
        get("/top-movies") {
            logger.info("GET /top-movies called")
            val topMovies = MongoDBAdapter.getTopMovies()
            call.respond(topMovies)
        }
        post("/analyze") {
            val req = call.receive<AnalyzeRequest>()
            val title = req.title.trim()
            val text  = req.text.trim()

            if (title.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Title is required and cannot be empty")
                return@post
            }
            if (text.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Text is required and cannot be empty")
                return@post
            }
            val loggedText = req.text
                .replace('\n', ' ')
                .take(200) // avoid huge log lines
            logger.info("POST /analyze called with title=$title, text=$loggedText")

            //val model = CustomSentimentModel()
            //val response: AnalyzeResponse = model.predictSentiment(title,text)
            val response = FineTunedSentimentModel.instance.predictSentiment(title, text)
            logger.info("POST /analyze called with text=$response")

            try {
                MongoDBAdapter.upsertAnalyzeResult(
                    source = "UI",
                    response = response
                )
            } catch (ex: Exception) {
                logger.error("Failed to persist analyze result for UI /analyze call", ex)
            }
            call.respond(response)
        }
        get("/metrics") {
            val serviceName = "data-analyzer-server"
            val uptimeSeconds = (System.currentTimeMillis() - appStartTimeMillis) / 1000
            val metrics = """
                # HELP app_uptime_seconds Application uptime in seconds.
                # TYPE app_uptime_seconds counter
                app_uptime_seconds{service="$serviceName"} $uptimeSeconds
            """.trimIndent()
            call.respondText(metrics, ContentType.Text.Plain)
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

    val port = System.getenv("PORT")?.toInt()?: 8080
    val logger = BasicJSONLoggerFactory.getLogger("DataAnalyzerServer")

    logger.info("Starting Review Consumer worker pool..")
    ReviewDataConsumer.start()

    logger.info("Starting Data Analyzer API on port $port")
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        analyzerModule()
    }.start(wait = true)
}
