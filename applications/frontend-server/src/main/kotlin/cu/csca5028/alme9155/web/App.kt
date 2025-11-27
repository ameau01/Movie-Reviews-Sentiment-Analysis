package cu.csca5028.alme9155.web

import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.http.content.staticResources
import io.ktor.server.http.content.*
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
import io.ktor.server.http.content.*
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
private val appStartTimeMillis: Long = System.currentTimeMillis()
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

            val params = call.receiveParameters()
            val text = params["text"]?.trim()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'text' parameter")
            val title = params["title"]?.trim()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'title' parameter")

            try {
                logger.info("Analyze URL: $analyzerUrl, parameter title=\"$title\", text=\"$text.\"")
                val response: HttpResponse = httpClient.post("$analyzerUrl/analyze") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToJsonElement(
                        mapOf(
                            "title" to title,
                            "text" to text
                        )).toString()
                    )
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
                        "title" to title,
                        "text" to text,
                        "labelText" to labelText,
                        "probabilities" to probabilities
                    )
                ))
            } catch (ex: Exception) {
                logger.error("Failed to call AI Movie Rating Services at $analyzerUrl/analyze", ex)
                call.respondText("BERT NLP Analyzer service unavailable", status = HttpStatusCode.ServiceUnavailable)
            }
        }
        staticResources("/static", "static")
        get("/") {
            logger.info("get / called.")
            call.respond(
                FreeMarkerContent(
                    "index.ftl",
                    mapOf("headers" to call.headersMap())
                )
            )
        }
        get("/report") {
            logger.info("GET /report called.")

            // sample output
            //[{"title":"On the Line: The Richard Williams Story","score":3.5},{"title":"One Flew Over the Cuckoo's Nest","score":3.1666666666666665},{"title":"Touch the Wall","score":3.0},{"title":"Sunset Boulevard","score":2.7857142857142856},{"title":"Pink Floyd: Live at Pompeii","score":2.75},{"title":"Fight Club","score":2.625},{"title":"The Human Condition III: A Soldier's Prayer","score":2.5},{"title":"Rapid Response","score":2.5},{"title":"The Lord of the Rings: The Two Towers","score":2.5},{"title":"The Empire Strikes Back","score":2.5}]

            try {
                logger.info("Report URL: $analyzerUrl/report")
                val response: HttpResponse = httpClient.get("$analyzerUrl/top-movies")
                val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray

                val topMovies = jsonArray.map { jsonElement ->
                    val jsonObj = jsonElement.jsonObject
                    val score = jsonObj["score"]!!.jsonPrimitive.double
                    object {
                        val title: String = jsonObj["title"]!!.jsonPrimitive.content
                        val score: Double = score
                        val scorePercent: Double = score * 20.0  // 0-5 → 0-100% for bar width
                    }
                }.sortedByDescending { it.score }.take(10)
                call.respond(FreeMarkerContent(
                    "report.ftl",
                    mapOf("topMovies" to topMovies)
                ))
            } catch (ex: Exception) {
                logger.error("Failed to call report service at $analyzerUrl/report", ex)
                call.respondText("Analyzer report service unavailable", status = HttpStatusCode.ServiceUnavailable)
            }
        }
        get("/health") {
            logger.info("get /health called.")
            call.respondText("OK", ContentType.Text.Plain)
        }
        get("/metrics") {
            val serviceName = "frontend-server"
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

    val port = System.getenv("PORT")?.toInt() ?: 8080
    val logger = BasicJSONLoggerFactory.getLogger("FrontendServer")

    logger.info("Starting Frontend Server on port $port")
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        frontendModule()
    }.start(wait = true)
}
