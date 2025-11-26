package cu.csca5028.alme9155.messaging

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document

import cu.csca5028.alme9155.database.MongoDBAdapter
import cu.csca5028.alme9155.database.RawMovieReview
import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory
import cu.csca5028.alme9155.sentiment.AnalyzeRequest

@Serializable
data class MovieReviewMessage(
    val title: String,
    val year: String?,
    val reviewText: String
)

data class PublishStats(
    val totalRawReviews: Int,
    val publishedCount: Int
)

data class SubscribeStats(
    val totalRawReviews: Int,
    val processedCount: Int
)

// RabbitMQ defaults
object RabbitMQConfig {
    const val RABBITMQ_HOST = "rabbitmq"
    const val RABBITMQ_PORT = 5672
    const val RABBITMQ_USER = "root"
    const val RABBITMQ_PASS = "password"
    const val RABBITMQ_EXCHANGE_NAME = "movie.reviews"
    const val RABBITMQ_QUEUE_NAME = "movie_reviews_queue"
    const val RABBITMQ_ROUTING_KEY = "reviews"    
}

/**
 * Class to handle review data stored collected from web API.
 * 
 * Publisher: Extract movie reviews persisted from NoSQL api_data collection and publish to message queue.
 * Subscriber: Listen to message queue and run sentiment analysis, then stored result to review_data NoSQL collection.
 */
object ReviewsDataHandler {
    private val exchange_name: String = System.getenv("MQ_EXCHANGE_NAME") ?: RabbitMQConfig.RABBITMQ_EXCHANGE_NAME
    private val queue_name: String = System.getenv("MQ_QUEUE_NAME") ?: RabbitMQConfig.RABBITMQ_QUEUE_NAME
    private val routing_key: String = System.getenv("MQ_ROUTING_KEY") ?: RabbitMQConfig.RABBITMQ_ROUTING_KEY

    private val json = Json { ignoreUnknownKeys = true }
    private val logger = BasicJSONLoggerFactory.getLogger("ReviewsDataHandler")

    private val connectionFactory = ConnectionFactory().apply {
        host = System.getenv("RABBITMQ_HOST") ?: RabbitMQConfig.RABBITMQ_HOST
        port = System.getenv("RABBITMQ_PORT")?.toIntOrNull() ?: RabbitMQConfig.RABBITMQ_PORT
        username = System.getenv("RABBITMQ_USER") ?: RabbitMQConfig.RABBITMQ_USER
        password = System.getenv("RABBITMQ_PASS") ?: RabbitMQConfig.RABBITMQ_PASS
    }

    private val connection: Connection by lazy { 
        logger.info("Connecting to RabbitMQ ...")
        connectionFactory.newConnection() 
    }

    private val channel: Channel by lazy {
        val ch = connection.createChannel()

        ch.exchangeDeclare(exchange_name, "direct", true)
        ch.queueDeclare(queue_name, true, false, false, null)
        ch.queueBind(queue_name, exchange_name, routing_key)
        logger.info("RabbitMQ exchange/queue/ binding ensured: $exchange_name -> $queue_name [$routing_key]")
        ch
    }

    fun publishAllRawReviews(): PublishStats {
        val allMovies = MongoDBAdapter.getAllRawMovieReviews()
        var publishedCount = 0

        logger.info("Starting to publish reviews from ${allMovies.size} movies...")

        for (movie in allMovies) {
            val doc: Document = movie.raw
            var title = doc.getString("title")?.trim().orEmpty()
            if (title.isBlank()) {
                title = doc.getString("alternativeTitle")?.trim().orEmpty()
            }
            if (title.isBlank()) title = "Unknown Movie"
            val year = doc.getString("year")?.trim()

            // Extract all review descriptions
            val descriptions = mutableListOf<String>()

            when (val reviewsField = doc.get("reviews")) {
                is Document -> {
                    for ((_, sourceValue) in reviewsField) {
                        if (sourceValue !is List<*>) continue

                        for (item in sourceValue) {
                            val desc = when (item) {
                                is Document -> item.getString("description")
                                is Map<*, *> -> item["description"] as? String
                                else -> null
                            } ?: continue

                            val text = desc.trim()
                            if (text.isEmpty()) continue

                            // ULTRA STRICT FILTER — reject ALL junk
                            if (
                                text.length < 50 ||  
                                text.endsWith(":") || 
                                text.contains("no reviews found", ignoreCase = true) ||
                                text.contains("movie reviews", ignoreCase = true) && text.length < 100 ||
                                text.contains("LA Weekly", ignoreCase = true) ||
                                text.contains("definitive source", ignoreCase = true) ||
                                text.contains("be the first to review", ignoreCase = true) ||
                                text.contains("write a review", ignoreCase = true) ||
                                text.contains("review this film", ignoreCase = true) ||
                                text.count { it.isLetterOrDigit() } < 30 
                            ) {
                                logger.debug("Skipping junk review: $text")
                                continue
                            }

                            descriptions += text
                        }
                    }
                }
            }
            if (descriptions.isNotEmpty()) {
                logger.info("Movie '$title' ($year) → ${descriptions.size} valid review(s) extracted and published")
                descriptions.take(2).forEachIndexed { i, text ->
                    val preview = if (text.length > 150) text.take(147) + "..." else text
                    logger.info("  [Review ${i + 1}] $preview")
                }
            } else {
                logger.info("Movie '$title' ($year) → no valid reviews after filtering")
            }

            // Publish one message per review
            for (text in descriptions) {
                val message = MovieReviewMessage(
                    title = title, 
                    year = year,
                    reviewText = text 
                )
                channel.basicPublish(
                    "",
                    queue_name,
                    null,
                    json.encodeToString(message).toByteArray()
                )
                publishedCount++
            }
        }

        logger.info("Successfully published $publishedCount review messages")
        return PublishStats(totalRawReviews = allMovies.size, publishedCount = publishedCount)
    }

    fun close() {
        try { channel.close() } catch (_: Exception) {}
        try { connection.close() } catch (_: Exception) {}
    }
}
