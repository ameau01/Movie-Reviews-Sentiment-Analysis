plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "cu.csca5028.alme9155.database"
version = "1.0.0"

dependencies {
    implementation(project(":support:logging-support"))
    implementation(project(":components:sentiment"))

    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.3.2")
    implementation("org.litote.kmongo:kmongo:5.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.4.14") 

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
    testImplementation("io.mockk:mockk:1.13.14")    
}
kotlin {
    jvmToolchain(21)
}


