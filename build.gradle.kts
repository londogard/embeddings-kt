plugins {
    java
    kotlin("jvm") version "1.3.72"
}

group = "com.londogard"
version = "1.0-SNAPSHOT"
val kluentVersion = 1.61

repositories {
    jcenter()
    google()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-dev/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.github.haifengl:smile-nlp:2.4.0")
    implementation("com.github.haifengl:smile-core:2.4.0")
    implementation("com.github.haifengl:smile-kotlin:2.4.0")

    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.72")
    // testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}