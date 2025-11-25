plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")    
}

group = "cu.csca5028.alme9155.collector"

application {
    version = rootProject.version as String
    applicationName = project.name    
    mainClass.set("cu.csca5028.alme9155.collector.AppKt")
}

dependencies {
    implementation(project(":support:workflow-support"))
    implementation(project(":support:logging-support"))
    implementation(project(":components:sentiment"))    
    implementation(project(":components:data-analyzer"))
    implementation(project(":components:database"))
    implementation(project(":components:api"))

    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.3.2")
    implementation("org.slf4j:slf4j-nop:2.0.16")
}
application {
    mainClass.set("cu.csca5028.alme9155.collector.AppKt")

    applicationName = project.name
    version = rootProject.version as String
}
tasks.withType<JavaExec>().configureEach {
    if (name == "run") {
        mainClass.set("cu.csca5028.alme9155.collector.AppKt")
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "cu.csca5028.alme9155.collector.AppKt"
    }
    // Turn the normal JAR into a runnable fat JAR
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}