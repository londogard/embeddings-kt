import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm") version "1.4.21"
    id("org.jetbrains.dokka") version "1.4.20"
}

group = "com.londogard"
version = "1.0-SNAPSHOT"
val kluentVersion: String by project
val smileVersion: String by project

repositories {
    jcenter()
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.londogard:londogard-nlp-toolkit:main-SNAPSHOT")
    implementation("com.github.haifengl:smile-nlp:$smileVersion")
    implementation("com.github.haifengl:smile-core:$smileVersion")
    implementation("com.github.haifengl:smile-kotlin:$smileVersion")

    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.4.21")
    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/londogard/embeddings-kt")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}
