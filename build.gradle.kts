import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.20"
    kotlin("jvm") version "1.4.21"
}

group = "com.londogard"
version = "1.0-SNAPSHOT"
val kluentVersion = "1.64"
val smileVersion = "2.6.0"

repositories {
    jcenter()
    google()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-dev/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.20")
    implementation("com.github.haifengl:smile-nlp:$smileVersion")
    implementation("com.github.haifengl:smile-core:$smileVersion")
    implementation("com.github.haifengl:smile-kotlin:$smileVersion")

    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.4.21")
    testImplementation("junit:junit:4.13.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
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
