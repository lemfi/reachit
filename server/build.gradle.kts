apply(plugin = "org.springframework.boot")

val springBootVersion: String by rootProject.extra

dependencies {

    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-websocket:$springBootVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
}