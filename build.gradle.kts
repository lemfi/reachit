import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val hopliteVersion: String by extra { "1.3.8" }

group = "fr.lemfi.reachit"
version = "0.0.1-SNAPSHOT"

val springBootVersion: String by extra { "2.2.6.RELEASE" }

buildscript {
    val kotlinVersion: String by extra { "1.5.0" }
    val springVersion: String by extra { "2.2.6.RELEASE" }

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.springframework.boot:spring-boot-gradle-plugin:$springVersion")
    }
}


subprojects {

    repositories {
        mavenLocal()
        mavenCentral()
    }

    apply(plugin = "kotlin")
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    compileKotlin.kotlinOptions.jvmTarget = "1.8"

    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    compileTestKotlin.kotlinOptions.jvmTarget = "1.8"

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect:1.7.21")
    }
}
