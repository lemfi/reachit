import org.gradle.api.file.DuplicatesStrategy

dependencies {

    implementation("io.ktor:ktor-server-core:2.1.0") {
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
    }

    implementation("org.slf4j:slf4j-simple:1.7.36")

    implementation("io.ktor:ktor-server-cio:1.6.1")
    implementation("io.ktor:ktor-websockets:1.6.1")
    implementation("io.ktor:ktor-jackson:1.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "fr.lemfi.reachit.server.ApplicationKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}