import org.springframework.boot.gradle.tasks.run.BootRun
import org.springframework.boot.gradle.tasks.bundling.BootJar

apply(plugin = "org.springframework.boot")

val springBootVersion: String by rootProject.extra

dependencies {

    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-websocket:$springBootVersion")

}

apply {
    from("$rootDir/developer.gradle.kts")
}
val developer: String? by extra

tasks.register<BootRun>("bootRunDev") {
    setGroup("application")
    dependsOn("assemble")

    val bootJarTask = tasks.getByName<BootJar>("bootJar")

    mainClass.set(provider { bootJarTask.mainClassName })
    classpath = bootJarTask.classpath

    doFirst {
        systemProperty("spring.profiles.active", "development")
        systemProperty("server.developer", developer ?: throw IllegalArgumentException("no developer found"))
    }
}