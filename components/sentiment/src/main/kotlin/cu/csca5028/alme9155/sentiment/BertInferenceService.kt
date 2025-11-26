package cu.csca5028.alme9155.sentiment

import kotlin.random.Random
import kotlinx.serialization.Serializable

import ai.djl.Application
import ai.djl.Model
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.inference.Predictor
import ai.djl.modality.Classifications
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel

import ai.djl.translate.TranslateException
import ai.djl.huggingface.translator.TextClassificationTranslatorFactory
import ai.djl.translate.Translator

import cu.csca5028.alme9155.logging.BasicJSONLoggerFactory  
import cu.csca5028.alme9155.logging.LogLevel

import java.nio.file.Paths

// Data class for NLP Sentiment Analysis
@Serializable
data class AnalyzeRequest(
    val title: String,
    val text: String
) {
    init {
        require(title.isNotBlank()) { "Title is required and must not be blank." }
        require(text.isNotBlank()) { "Text is required and must not be blank." }
    }
}

// Stable response type used by all services
@Serializable
data class AnalyzeResponse(
    val title: String,
    val text: String,
    val labelId: Int,
    val labelText: String,
    val probabilities: Map<String, Double>
)

// Rating Labels
val SENTIMENT_LABELS = listOf(
    "very negative",
    "negative",
    "neutral",
    "positive",
    "very positive"
)

private const val MODEL_DIR = "models/distilbert-sst5-finetuned-v3"

/**
 * Sentiment model to build UI service before applying real pre-trained model.
 * Returns an AnalyzeResponse.
 */
class CustomSentimentModel(
    private val labels: List<String> = SENTIMENT_LABELS
) {

    /**
     * Static model
     */
    fun predictSentiment(title: String, text: String): AnalyzeResponse {
        val raw = labels.map { Random.nextDouble(0.01, 1.0) }
        val sum = raw.sum()

        val probs = raw.map { it / sum }

        val maxIndex = probs.indices.maxByOrNull { probs[it] } ?: 0

        val probabilities = labels.indices.associate { i ->
            labels[i] to probs[i]
        }

        return AnalyzeResponse(
            title = title,
            text = text,
            labelId = maxIndex,
            labelText = labels[maxIndex],
            probabilities = probabilities
        )
    }
}

private val logger = BasicJSONLoggerFactory.getLogger("BertInferenceService")

/**
 * Load Fine-tuned DistilBERT model from local folder 
 */
class FineTunedSentimentModel private constructor() {

    private var model: ZooModel<String, Classifications>? = null
    private var predictor: Predictor<String, Classifications>? = null
    private var initError: Throwable? = null

    init {
        logger.info("Loading fine-tuned DistilBERT alme9155/Model from ./$MODEL_DIR ...")
        val modelPath = Paths.get(MODEL_DIR).toAbsolutePath()
        logger.info("modelPath: $modelPath")

        try {
            val criteria = Criteria.builder()
                .optApplication(Application.NLP.TEXT_CLASSIFICATION)
                .setTypes(String::class.java, Classifications::class.java)
                .optModelPath(modelPath)
                .build()

            val loadedModel = criteria.loadModel()
            val newPredictor = loadedModel.newPredictor()

            // warm up instance
            newPredictor.predict("This is a warm-up sentence to load the model.")
            model = loadedModel
            predictor = newPredictor
            logger.info("Fine-tuned model loaded and ready!")
        } catch (ex: Exception) {
            initError = ex
            //logger.error("Warning: Warm-up failed: ${ex.message}", ex)
        }
    }

    fun predictSentiment(title: String, text: String): AnalyzeResponse {
        logger.info("predictSentiment() called.")
        val p = predictor
        if (p == null) {
            //logger.error(
            //    "Fine-tuned model unavailable, using CustomSentimentModel fallback.",
            //    initError
            //)
            return CustomSentimentModel().predictSentiment(title, text)
        } else {
            return try {
                val result = p!!.predict(text)
                val items = result.items<Classifications.Classification>()

                val probabilities = items.associate {
                    val id = it.className.toInt()
                    SENTIMENT_LABELS[id.coerceIn(0, 4)] to it.probability
                }

                val best = result.best<Classifications.Classification>()
                val bestId = best.className.toInt()
                val bestLabel = SENTIMENT_LABELS[bestId.coerceIn(0, 4)]

                AnalyzeResponse(
                    title = title,
                    text = text,
                    labelId = bestId,
                    labelText = bestLabel,
                    probabilities = probabilities
                )
            } catch (ex: Exception) {
                //logger.error("prediction failure", ex)
                CustomSentimentModel().predictSentiment(title, text)
            }
        }
    }

    // Singletone
    companion object {
        val instance: FineTunedSentimentModel by lazy { FineTunedSentimentModel() }
    }
}

//class HuggingFaceTranslatorFactory(private val tokenizer: HuggingFaceTokenizer) :
//    TextClassificationTranslatorFactory() {
//
//    override fun newInstance(model: ai.djl.Model): Translator<String, Classifications> {
//        return super.newInstance(model).also { translator ->
//            // Force DJL to use our pre-loaded tokenizer instead of downloading one
//            val field = translator.javaClass.superclass.getDeclaredField("tokenizer")
//            field.isAccessible = true
//            field.set(translator, tokenizer)
//        }
//    }
//}