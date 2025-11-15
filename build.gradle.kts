import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

// project build file.
plugins {
    kotlin("jvm") version "2.2.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21" apply false
}

version = "1.0.0"
val ktorVersion = "3.3.2"

allprojects {
    group = "cu.csca5028.alme9155"
    repositories {
        mavenCentral() 
    }
}

subprojects {
    pluginManager.apply("org.jetbrains.kotlin.jvm")
    pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

    val implementation by configurations
    val testImplementation by configurations

    dependencies {
        implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
        implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
        implementation("io.ktor:ktor-server-freemarker-jvm:$ktorVersion")
        implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")

        testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-test")
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
