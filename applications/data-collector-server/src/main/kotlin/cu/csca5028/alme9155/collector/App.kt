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

fun Application.collectorModule() {
    routing {
        /*
        get("/") {
            call.respondText("Data Collector API", ContentType.Text.Plain)
        }
        */
        get("/") {
            val usage = """
                AI-Powered Movie Sentiment Rating System
                ------------------------------------------
                Data Collector API
                
                Usage:
                - POST /collect
                    * Content-Type: application/json
                    * Body: { "URL": "URL to file download" }
                    
                Example (curl):
                curl -X POST http://localhost:8080/collect \
                    -H "Content-Type: application/json" \
                    -d '{"URL": "https://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz"}' 
                    
                

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
    embeddedServer(
        factory = Netty,
        port = port, 
        module = Application::collectorModule
    ).start(wait = true)
}