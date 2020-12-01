apply(plugin = "org.springframework.boot")

val springBootVersion: String by rootProject.extra

dependencies {

    implementation("org.slf4j:slf4j-api:1.7.28")
    implementation("org.slf4j:slf4j-simple:1.7.28")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")

    implementation("com.github.lemfi.kest:core:0.0.2")
    implementation("com.github.lemfi.kest:step-http:0.0.2")
    implementation("com.github.lemfi.kest:junit5:0.0.2")
}