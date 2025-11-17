package cu.csca5028.alme9155.analyzer

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.util.TimeZone
import io.ktor.server.application.*
import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory  

private val logger = BasicJSONLoggerFactory.getLogger("DataAnalyzerServer")

fun Application.analyzerModule() {
    val port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt()
        ?: System.getenv("PORT")?.toInt()
        ?: 8080

    routing {
        get("/") {
            val usage = """
                AI-Powered Movie Sentiment Rating System
                ------------------------------------------
                Data Analyzer API
                Running on: http://localhost:$port

                Usage:
                - POST /analyze
                    * Content-Type: application/json
                    * Body: { "text": "your movie review here" }
                    
                Example (curl):
                curl -X POST http://localhost:$port/analyze \
                    -H "Content-Type: application/json" \
                    -d '{"text": "I loved this movie!"}' 
                    
                Health check:
                curl http://localhost:$port/health

            """.trimIndent()

            call.respondText(usage, ContentType.Text.Plain)
        }
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain)
        }
    }
}

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt()?: 8080
    val logger = BasicJSONLoggerFactory.getLogger("DataAnalyzerServer")

    logger.info("Starting Data Analyzer API on port $port")
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        analyzerModule()
    }.start(wait = true)
}
