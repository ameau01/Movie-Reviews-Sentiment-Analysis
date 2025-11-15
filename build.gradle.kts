version = "1.0.0"

plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20" apply false
}

allprojects {
    group = "cu.csca5028.alme9155"
    repositories {
        mavenCentral() 
    }
}

subprojects {
    apply(plugin = "kotlin")

    val ktorVersion: String by project

    dependencies {
        implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
        implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
        implementation("io.ktor:ktor-server-freemarker-jvm:$ktorVersion")
        implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
        
        testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-test")
    }

    kotlin {
        jvmToolchain(21)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
