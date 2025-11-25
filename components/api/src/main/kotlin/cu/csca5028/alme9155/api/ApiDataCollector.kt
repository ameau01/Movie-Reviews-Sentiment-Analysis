package cu.csca5028.alme9155.api

import cu.csca5028.alme9155.database.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import org.bson.Document

import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory  
import cu.csca5028.alme9155.logging.LogLevel

// External API defaults
object APIDataConfig {
    const val RAPID_API_URL = "https://film-show-ratings.p.rapidapi.com/items/?type=film"
    const val RAPID_API_KEY = "114fce61cdmsh31ce360533f7187p1da47bjsn7c2abf7d6e02"
    const val RAPID_API_HOST = "film-show-ratings.p.rapidapi.com"
}

object ApiDataCollector {
    private val client = HttpClient {
        install(ContentNegotiation) { json() }

        install(ContentEncoding) {
            gzip()
            deflate()
        }

        expectSuccess = true
    }

    private val apiUrl: String = System.getenv("RAPID_API_URL")?: APIDataConfig.RAPID_API_URL
    private val apiKey: String = System.getenv("RAPID_API_KEY")?: APIDataConfig.RAPID_API_KEY
    private val apiHost: String = System.getenv("RAPID_API_HOST")?: APIDataConfig.RAPID_API_HOST

    private var lastFetchedMovies: List<RawMovieReview> = emptyList()

    suspend fun fetchDataFromAPI(): Int = withContext(Dispatchers.IO) {
        println("Fetching API Data from: $apiUrl ...")

        val response: String = client.get(apiUrl) {
            headers {
                append("x-rapidapi-key", apiKey)
                append("x-rapidapi-host", apiHost)
            }
        }.body()

        println("----------------- Fetched API Data Preview -----------------")
        val preview = response.take(3000).let { 
            if (response.length > 3000) "$it..." else it 
        }
        println("Response preview (first 3000 chars): $preview")
        println("---------------------------------------------------------")


        val json = Json.parseToJsonElement(response)
        val array = json.jsonObject["result"]?.jsonArray ?: json.jsonArray

        val movies = array.mapNotNull { elem ->
            val obj = elem.jsonObject
            val title = obj["title"]?.jsonPrimitive?.contentOrNull
            val year = obj["year"]?.jsonPrimitive?.contentOrNull
            val id = obj["id"]?.jsonPrimitive?.contentOrNull
                ?: obj["movie_id"]?.jsonPrimitive?.contentOrNull

            val movieId = id ?: listOfNotNull(title, year).joinToString("_")
                .takeIf { it.isNotBlank() } ?: return@mapNotNull null

            val doc = Document.parse(obj.toString())
            RawMovieReview(movieId = movieId, raw = doc)
        }

        lastFetchedMovies = movies
        println("... Successfully fetched ${movies.size} records from API end point.")
        movies.size
    }

    fun getFetchedData(): List<RawMovieReview> = lastFetchedMovies.toList()

    fun close() {
        client.close()
    }
}
