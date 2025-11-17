package cu.csca5028.alme9155.collector

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

private val logger = BasicJSONLoggerFactory.getLogger("DataCollectorServer")

fun Application.collectorModule() {
    val port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt()
        ?: System.getenv("PORT")?.toInt()
        ?: 8080

    routing {
        get("/") {
            val usage = """
                AI-Powered Movie Sentiment Rating System
                ------------------------------------------
                Data Collector API
                Running on: http://localhost:$port
                
                Usage:
                - POST /collect
                    * Content-Type: application/json
                    * Body: { "URL": "URL to file download" }
                    
                Example (curl):
                curl -X POST http://localhost:$port/collect \
                    -H "Content-Type: application/json" \
                    -d '{"URL": "https://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz"}' 
                    
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
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val logger = BasicJSONLoggerFactory.getLogger("DataCollectorServer")

    logger.info("Starting Data Collector API on port $port")
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        collectorModule()
    }.start(wait = true)
}