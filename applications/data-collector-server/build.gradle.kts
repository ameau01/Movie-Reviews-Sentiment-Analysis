plugins {
    application
}

group = "cu.csca5028.alme9155.collector"

application {
    mainClass.set("cu.csca5028.alme9155.collector.AppKt")
}

dependencies {
    implementation(project(":components:data-analyzer"))
    implementation(project(":support:workflow-support"))
    implementation(project(":support:logging-support"))

}
