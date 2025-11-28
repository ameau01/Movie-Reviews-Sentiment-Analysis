package cu.csca5028.alme9155.sentiment

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BertInferenceServiceTest {

    @Test
    fun testAnalyzeRequestEmptyTitle() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            AnalyzeRequest(title = "   ", text = "Some text")
        }
        assertTrue(ex.message!!.contains("Title is required"))
    }

    @Test
    fun testAnalyzeRequestEmptyReviewText() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            AnalyzeRequest(title = "Some title", text = "   ")
        }
        assertTrue(ex.message!!.contains("Text is required"))
    }

    @Test
    fun testValidAnalyzeResponse() {
        val title = "The Empire Strikes Back"
        val text = "A classic sci-fi sequel with strong character development."

        val response = FineTunedSentimentModel.instance.predictSentiment(title, text)

        assertEquals(title, response.title)
        assertEquals(text, response.text)
        assertTrue(response.labelId in 0..4)
        assertTrue(response.labelText in SENTIMENT_LABELS)
        assertFalse(response.probabilities.isEmpty())

        val probs = response.probabilities.values
        probs.forEach { p ->
            assertTrue(p >= 0.0)
            assertTrue(p <= 1.0)
        }

        val sum = probs.sum()
        assertTrue(sum in 0.95..1.05, "Total should add up to 1.0, was $sum")
    }
}
