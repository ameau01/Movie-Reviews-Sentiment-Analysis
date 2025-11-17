package cu.csca5028.alme9155.web

import freemarker.cache.ClassTemplateLoader
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.util.pipeline.PipelineContext
import java.util.TimeZone
import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory  

private val logger = BasicJSONLoggerFactory.getLogger("FrontendServer")

fun Application.frontendModule() {
    logger.info("starting the app")

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    routing {
        get("/") {
            call.respond(
                FreeMarkerContent(
                    "index.ftl",
                    mapOf("headers" to call.headersMap())
                )
            )
        }
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain)
        }
        staticResources("/static/styles", "static/styles")
        staticResources("/static/images", "static/images")
    }
}

private fun ApplicationCall.headersMap(): Map<String, String> =
    request.headers.entries().associate { (key, value) -> key to value.joinToString() 
}


fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val logger = BasicJSONLoggerFactory.getLogger("FrontendServer")

    logger.info("Starting Frontend Server on port $port")
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        frontendModule()
    }.start(wait = true)
}
