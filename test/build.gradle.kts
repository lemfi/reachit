dependencies {

    implementation("org.slf4j:slf4j-api:1.7.28")
    implementation("org.slf4j:slf4j-simple:1.7.28")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")

    implementation("com.github.lemfi.kest:core:0.1.5")
    implementation("com.github.lemfi.kest:step-http:0.6.8")
    implementation("com.github.lemfi.kest:junit5:0.1.5")
}


tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}