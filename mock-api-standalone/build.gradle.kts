plugins {
    kotlin("jvm") version "1.9.20"
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.example.mockapi.ApplicationKt")
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.6")
    implementation("io.ktor:ktor-server-netty:2.3.6")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-gson:2.3.6")
    implementation("io.ktor:ktor-server-cors:2.3.6")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.auth0:java-jwt:4.4.0")
}
