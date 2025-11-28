package cu.csca5028.alme9155.api

import cu.csca5028.alme9155.database.RawMovieReview
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.bson.Document
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApiDataCollectorTest {

    private fun getFetchedDataFromApi(): List<RawMovieReview> {
        val jsonString = javaClass.classLoader
            .getResource("api-response.json")!!
            .readText()

        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(jsonString)
        val array = root.jsonObject["result"]?.jsonArray ?: root.jsonArray

        return array.mapNotNull { element ->
            val obj = element.jsonObject
            val title = obj["title"]?.toString()?.removeSurrounding("\"")
            val year = obj["year"]?.toString()?.removeSurrounding("\"")
            val id = obj["id"]?.toString()?.removeSurrounding("\"")
                ?: obj["movie_id"]?.toString()?.removeSurrounding("\"")
            val movieId = id ?: listOfNotNull(title, year).joinToString("_")
                .takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val doc = Document.parse(obj.toString())
            RawMovieReview(movieId = movieId, raw = doc)
        }
    }

    @Test
    fun testAPIResponse() {
        val movies = getFetchedDataFromApi()

        assertTrue(movies.isNotEmpty(), "Should load at least one movie")
        assertTrue(movies.size >= 2, "Expected multiple movies")

        val iSwear = movies.find { it.movieId == "I Swear_2025" }
        assertNotNull(iSwear, "Should find 'I Swear'")
        assertEquals("I Swear", iSwear.raw.getString("title"))
        assertEquals("2025", iSwear.raw.getString("year"))

        val unknownMovie = movies.find { it.movieId.contains("The Unknowns") }
        assertNotNull(unknownMovie, "Should handle fallback movieId")
        assertEquals("The Unknowns", unknownMovie.raw.getString("title"))
    }
}