rootProject.name = "AI-Powered Movie Sentiment Rating System"

include(
    "applications:frontend-server",
    "applications:data-analyzer-server",
    "applications:data-collector-server",

    "components:data-collector",
    "components:data-analyzer",
    "components:sentiment",
    "components:database",
    "components:api",

    "support:logging-support",
    "support:workflow-support"
)
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()   // ← THIS LINE IS REQUIRED FOR SHADOW
    }
}