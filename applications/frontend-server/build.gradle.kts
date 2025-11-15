plugins {
    application
}

group = "cu.csca5028.alme9155.web"

val ktorVersion: String by project

dependencies {
    implementation(project(":components:data-analyzer"))
    implementation(project(":support:logging-support"))
    implementation(project(":support:workflow-support"))

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-freemarker-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
}
