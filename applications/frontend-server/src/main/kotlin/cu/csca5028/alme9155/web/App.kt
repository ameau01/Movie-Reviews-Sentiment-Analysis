package cu.csca5028.alme9155.web

import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import io.ktor.util.pipeline.PipelineContext
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import freemarker.template.Configuration
import freemarker.cache.ClassTemplateLoader
import java.util.TimeZone
import java.io.StringReader

import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory
import cu.csca5028.alme9155.logging.LogLevel

private val logger = BasicJSONLoggerFactory.getLogger("FrontendServer")
private val httpClient = HttpClient(CIO)
private val SENTIMENT_LABELS = listOf(
    "very negative", "negative", "neutral", "positive", "very positive"
)
private fun ApplicationCall.headersMap(): Map<String, String> =
    request.headers.entries().associate { (key, value) -> key to value.joinToString() 
}

fun Application.frontendModule() {
    val analyzerUrl = (System.getenv("ANALYZER_URL") ?: "http://data-analyzer:8080/analyze").removeSuffix("/analyze")    
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(
            this::class.java.classLoader, 
            "templates"
        )
    }
    routing {
        post("/analyze") {
            logger.info("POST /analyze called.")
            logger.info("Forwarding request to BERT NLP engine using URL = $analyzerUrl.")

            val text = call.receiveParameters()["text"]?.trim()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'text' parameter")
            logger.info("POST /analyze called, parameter text='$text.'")

            try {
                logger.info("Analyze URL: $analyzerUrl, Review text: $text")
                val response: HttpResponse = httpClient.post("$analyzerUrl/analyze") {
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody("text=$text")
                }
                val jsonObj = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val labelText = jsonObj["labelText"]!!.jsonPrimitive.content
                //val predictedLabel = jsonObj["predicted_label"]!!.jsonPrimitive.double.toFloat()
                val probabilitiesMap = jsonObj["probabilities"]!!.jsonObject               
                val probabilities = SENTIMENT_LABELS.map { 
                    label -> val value = probabilitiesMap[label]!!.jsonPrimitive.double
                    object {
                        val label = label
                        val value = value
                    }
                }.sortedByDescending { it.value }

                call.respond(FreeMarkerContent(
                    "results.ftl", 
                    mapOf(
                        "text" to text,
                        "probabilities" to probabilities
                    )
                ))
            } catch (ex: Exception) {
                logger.error("Failed to call AI Movie Rating Services at $analyzerUrl/analyze", ex)
                call.respondText("BERT NLP Analyzer service unavailable", status = HttpStatusCode.ServiceUnavailable)
            }
        }
        get("/") {
            logger.info("get / called.")
            call.respond(
                FreeMarkerContent(
                    "index.ftl",
                    mapOf("headers" to call.headersMap())
                )
            )
        }
        get("/health") {
            logger.info("get /health called.")
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
    val logger = BasicJSONLoggerFactory.getLogger("FrontendServer")

    logger.info("Starting Frontend Server on port $port")
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        frontendModule()
    }.start(wait = true)
}
