import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    id("org.jetbrains.dokka") version "0.9.17"
    kotlin("jvm") version "1.4.30-253"
}

group = "com.londogard"
version = "1.0-SNAPSHOT"
val kluentVersion = "1.61"
val smileVersion = "2.4.0"

repositories {
    jcenter()
    google()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-dev/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.github.haifengl:smile-nlp:$smileVersion")
    implementation("com.github.haifengl:smile-core:$smileVersion")
    implementation("com.github.haifengl:smile-kotlin:$smileVersion")

    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.72")
    testImplementation("junit:junit:4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(tasks.dokka)
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
