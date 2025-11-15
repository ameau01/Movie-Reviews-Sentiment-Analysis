package cu.csca5028.alme9155.web

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppTest {
    @Test
    fun testEmptyHome() = testApplication {
        application { 
            frontendModule() 
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(
            body.isNotBlank(),
            "Home page body should not be blank"
        )


        //assertContains(response.bodyAsText(), "AI-Powered Movie Sentiment Rating System")
    }
}
