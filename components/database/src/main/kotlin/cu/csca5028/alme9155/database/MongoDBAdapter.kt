package cu.csca5028.alme9155.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document
import org.litote.kmongo.KMongo

import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory  
import cu.csca5028.alme9155.logging.LogLevel
import cu.csca5028.alme9155.sentiment.AnalyzeResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class RawMovieReview(
    val movieId: String,
    val raw: Document
)

@Serializable
data class AnalyzeResultData(
    val title: String,
    val normalizedTitle: String,
    val text: String,
    val labelId: Int,
    val labelText: String,
    val probabilities: Map<String, Double>,
    val source: String,                    // "UI", "API", "MQ", etc.
    val analyzedAt: Long = System.currentTimeMillis()
)


// MongoDB defaults
object DBConfig {
    const val MONGO_URI = "mongodb://root:password@mongodb:27017/sentiment_db?authSource=admin"
    const val DB_NAME = "sentiment_db"
    const val API_DATA_COLLECTION = "api_data"
    const val REVIEWS_COLLECTION = "reviews_data"
}

private val logger = BasicJSONLoggerFactory.getLogger("MongoDBAdapter")

object MongoDBAdapter {
    private val uri: String = System.getenv("MONGODB_URI")?: DBConfig.MONGO_URI
    private val dbName: String = System.getenv("MONGODB_DB_NAME") ?: DBConfig.DB_NAME
    private val apiCollectionName: String = System.getenv("API_DATA_COLLECTION") ?: DBConfig.API_DATA_COLLECTION
    private val reviewsCollectionName: String = System.getenv("REVIEWS_COLLECTION") ?: DBConfig.REVIEWS_COLLECTION

    private val client: MongoClient by lazy {
        KMongo.createClient(uri)
    }
    private val database: MongoDatabase by lazy {
        client.getDatabase(dbName)
    }
    private val apiCollection: MongoCollection<Document> by lazy {
        database.getCollection(apiCollectionName)
    }
    private val reviewsCollection: MongoCollection<Document> by lazy {
        database.getCollection(reviewsCollectionName)
    }

    private val json = Json { ignoreUnknownKeys = true }
    private fun normalizeTitle(title: String): String = title.trim().lowercase().replace(Regex("\\s+"), " ")

    /** 
     * Upsert a list of movie reviews into MongoDB
     * @return number of documents upserted
     */
    fun upsertMoviesReviews(reviews: List<RawMovieReview>): Int {
        if (reviews.isEmpty()) return 0

        logger.info("Upserting ${reviews.size} documents into '$apiCollectionName' collection ...")
        val options = ReplaceOptions().upsert(true)
        var count = 0

        for (movie in reviews) {
            val doc = Document(movie.raw.toMutableMap()).apply {
                put("movie_id", movie.movieId)
                put("_fetchedAt", System.currentTimeMillis())
            }

            apiCollection.replaceOne(Document("movie_id", movie.movieId), doc, options)
            count++
        }

        logger.info("... Successfully upserted $count documents to database.")
        return count
    }

    /**
     * Retrieve all raw movie reviews from the api_data collection.
     */
    fun getAllRawMovieReviews(): List<RawMovieReview> {
        val reviews = mutableListOf<RawMovieReview>()

        logger.info("Fetching all raw review documents from '$apiCollectionName' collection ...")

        for (doc in apiCollection.find()) {
            val movieId = doc.getString("movie_id") ?: continue
            reviews += RawMovieReview(movieId = movieId, raw = doc)
        }

        logger.info("... Fetched ${reviews.size} documents from '$apiCollectionName'.")
        return reviews
    }

    /**
     * Convert AnalyzeResponse to data object to be persisted in DB.
     */
    private fun ConvertToAnalyzeResultData(source: String, response: AnalyzeResponse): AnalyzeResultData {
        val trimmedTitle = response.title.trim()
        val normalized = normalizeTitle(trimmedTitle)

        return AnalyzeResultData(
            title = trimmedTitle,
            normalizedTitle = normalized,
            text = response.text,
            labelId = response.labelId,
            labelText = response.labelText,
            probabilities = response.probabilities,
            source = source,
            analyzedAt = System.currentTimeMillis()
        )
    }

    /**
     * Upsert analyze result to review_data collection in MongoDB.
     * Source: UI, or MQ
     */
    fun upsertAnalyzeResult(source: String, response: AnalyzeResponse): Int {
        val trimmedTitle = response.title.trim()
        if (trimmedTitle.isBlank()) {
            logger.warn("Skipping analyze result upsert: title is blank")
            return 0
        }

        val data = ConvertToAnalyzeResultData(source, response)

        val docJson = json.encodeToString(AnalyzeResultData.serializer(), data)
        val doc = Document.parse(docJson)

        val filter = Document("normalizedTitle", data.normalizedTitle)
            .append("source", data.source)

        val options = ReplaceOptions().upsert(true)

        reviewsCollection.replaceOne(filter, doc, options)
        logger.info(
            "Upserted AnalyzeResultData for normalizedTitle='${data.normalizedTitle}', source='${data.source}'"
        )
        return 1
    }



    fun close() {
        client.close()
    }
}