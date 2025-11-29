package cu.csca5028.alme9155

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

private const val FRONTEND_URL: String = "http://localhost"
private const val ANALYZER_URL: String = "http://localhost:8881"
private const val COLLECTOR_URL: String = "http://localhost:8882"

/**
 * End-to-end integration tests that all service is running (docker compose up -d)
 */
class WorkflowIntegrationTest {
    private val client = HttpClient(CIO)

    /**
     * Integration Workflow:
     * UI Form -> POST /analyze (frontend) -> data-analyzer -> HTML result page.
     *
     * UI Form submit:
     *  - title: "god father 2"
     *  - text:  "fantastic"
     */
    @Test
    fun testUIWorkflow() = runBlocking {
        val response: HttpResponse = client.post("$FRONTEND_URL/analyze") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "title" to "god father 2",
                    "text" to "fantastic"
                ).formUrlEncode()
            )
        }

        assertEquals(
            expected = 200,
            actual = response.status.value,
            message = "Frontend /analyze should return 200."
        )

        val body = response.bodyAsText()
        assertTrue(
            body.contains("Analysis Result"),
            "Response page should contain the 'Analysis Result' heading"
        )

        assertTrue(
            body.contains("Movie Title:"),
            "Response page should show the 'Movie Title:' label"
        )
        assertTrue(
            body.contains("god father 2"),
            "Response page should contain the submitted movie title"
        )

        assertTrue(
            body.contains("Your Review Text:"),
            "Response page should show the 'Your Review Text:' label"
        )
        assertTrue(
            body.contains("fantastic", ignoreCase = true),
            "Response page should contain the submitted review text"
        )

        assertTrue(
            body.contains("AI Movie Rating Based on your review:", ignoreCase = true),
            "Response should contain the AI rating heading"
        )
        val hasAnyLabel =
            body.contains("VERY NEGATIVE", ignoreCase = true) ||
            body.contains("VERY POSITIVE", ignoreCase = true) ||
            body.contains("negative", ignoreCase = true) ||
            body.contains("positive", ignoreCase = true)

        assertTrue(
            hasAnyLabel,
            "Response page should show at least one sentiment label (e.g., VERY NEGATIVE)"
        )
        assertTrue(
            body.contains("All Confidence Scores:", ignoreCase = true),
            "Response page should show the 'All Confidence Scores:' section"
        )
    }
}
