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

fun Application.analyzerModule() {
    routing {
        get("/") {
            val usage = """
                AI-Powered Movie Sentiment Rating System
                ------------------------------------------
                Data Analyzer API
                
                Usage:
                - POST /analyze
                    * Content-Type: application/json
                    * Body: { "text": "your movie review here" }
                    
                Example (curl):
                curl -X POST http://localhost:8080/analyze \
                    -H "Content-Type: application/json" \
                    -d '{"text": "I loved this movie!"}' 
                    
                

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
        module = Application::analyzerModule
    ).start(wait = true)
}
