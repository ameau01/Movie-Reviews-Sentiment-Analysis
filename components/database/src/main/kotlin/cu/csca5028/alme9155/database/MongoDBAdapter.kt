package cu.csca5028.alme9155.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document
import org.litote.kmongo.KMongo

import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory  
import cu.csca5028.alme9155.logging.LogLevel

data class RawMovieReview(
    val movieId: String,
    val raw: Document
)

// MongoDB defaults
object DBConfig {
    const val MONGO_URI = "mongodb://root:password@mongodb:27017/sentiment_db?authSource=admin"
    const val DB_NAME = "sentiment_db"
    const val API_DATA_COLLECTION = "api_data"
    const val REVIEWS_COLLECTION = "reviews_data"
    const val REVIEWS_SUMMARY_COLLECTION = "reviews_summary_data"
}

private val logger = BasicJSONLoggerFactory.getLogger("MongoDBAdapter")

object MongoDBAdapter {
    private val uri: String = System.getenv("MONGODB_URI")?: DBConfig.MONGO_URI
    private val dbName: String = System.getenv("MONGODB_DB_NAME") ?: DBConfig.DB_NAME
    private val collectionName: String = System.getenv("API_DATA_COLLECTION") ?: DBConfig.API_DATA_COLLECTION

    private val client: MongoClient by lazy {
        KMongo.createClient(uri)
    }
    private val database: MongoDatabase by lazy {
        client.getDatabase(dbName)
    }
    private val collection: MongoCollection<Document> by lazy {
        database.getCollection(collectionName)
    }

    /** 
     * Upsert a list of movie reviews into MongoDB
     * @return number of documents upserted
     */
    fun upsertMoviesReviews(reviews: List<RawMovieReview>): Int {
        if (reviews.isEmpty()) return 0

        logger.info("Upserting ${reviews.size} documents into '$collectionName' collection ...")
        val options = ReplaceOptions().upsert(true)
        var count = 0

        for (movie in reviews) {
            val doc = Document(movie.raw.toMutableMap()).apply {
                put("movie_id", movie.movieId)
                put("_fetchedAt", System.currentTimeMillis())
            }

            collection.replaceOne(Document("movie_id", movie.movieId), doc, options)
            count++
        }

        logger.info("... Successfully upserted $count documents to database.")
        return count
    }

    fun close() {
        client.close()
    }
}