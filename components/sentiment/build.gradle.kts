import java.net.URL
import java.io.InputStream

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}
group = "cu.csca5028.alme9155.sentiment"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(kotlin("test"))

    implementation("ai.djl:api:0.30.0")
    implementation("ai.djl.huggingface:tokenizers:0.30.0")
    //implementation("ai.djl.pytorch:pytorch-engine:0.30.0")
    //implementation("ai.djl.pytorch:pytorch-native-cpu:0.30.0")
    runtimeOnly("ai.djl.pytorch:pytorch-engine:0.30.0")
    runtimeOnly("ai.djl.pytorch:pytorch-native-auto:1.9.1")

    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation(project(":support:logging-support"))
}

// ─────────────────────────────────────────────────────────────────────────────
// Download model from Hugging Face if not present
// collab notebook can be viewed in 
//   https://github.com/alme9155/csca-5028-sentiment-analysis/blob/main/docs/Movie_Ratings_Sentiment_Analysis_v5.ipynb
// ─────────────────────────────────────────────────────────────────────────────
val downloadCustomModel by tasks.registering {
    // This is relative to the *sentiment* module directory:
    // components/sentiment/models/distilbert-sst5-finetuned-v3
    val modelDir = layout.projectDirectory.dir("models/distilbert-sst5-finetuned-v3")

    // Marker file: if present, we assume the model is already downloaded
    val markerFile = modelDir.file("model.safetensors").asFile

    // Your custom HF repo base URL
    val baseUrl =
        "https://huggingface.co/alme9155/distilbert-sst5-finetuned-v3/resolve/main"

    val files = listOf(
        "config.json",
        "tokenizer.json",
        "tokenizer_config.json",
        "special_tokens_map.json",
        "vocab.txt",
        "model.safetensors",
        "training_args.bin"
    )

    outputs.dir(modelDir)

    doLast {
        // Ensure directory exists
        modelDir.asFile.mkdirs()

        if (markerFile.exists()) {
            println("Model marker file '${markerFile.name}' already exists, skipping download.")
            return@doLast
        }

        println("Downloading DistilBERT SST-5 model into ${modelDir.asFile}")

        files.forEach { name ->
            val target = modelDir.file(name).asFile

            if (target.exists()) {
                println("  [SKIP] $name already exists")
                return@forEach
            }

            val url = "$baseUrl/$name"
            println("  [GET ] $url")

            try {
                val urlObj: URL = URL(url)
                urlObj.openStream().use { input: InputStream ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                println("  [OK  ] Saved $name")
            } catch (e: Exception) {
                println("  [WARN] Could not download $name: ${e.message}")
            }
        }
    }
}

tasks.withType<Test> { dependsOn(downloadCustomModel) }
tasks.named("processResources") { dependsOn(downloadCustomModel) }
tasks.named("classes") { dependsOn(downloadCustomModel) }