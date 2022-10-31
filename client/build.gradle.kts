val hopliteVersion: String by project.rootProject.extra

plugins {
    application
}

application {
    mainClass.set("fr.lemfi.reachit.client.ReachitClientKt")
}
dependencies {

    implementation("io.ktor:ktor-client-websockets:1.6.1")
    implementation("io.ktor:ktor-client-core:1.6.1")
    implementation("io.ktor:ktor-client-java:1.6.1")

    implementation("org.slf4j:slf4j-simple:1.7.36")

    implementation("io.ktor:ktor-client-jackson:2.1.3")

    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    implementation("com.sksamuel.hoplite:hoplite-yaml:$hopliteVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
}