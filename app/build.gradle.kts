plugins {
    id("java")
    application
    checkstyle
    jacoco
    id("io.freefair.lombok") version "8.4"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass = "hexlet.code.App"
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("gg.jte:jte:3.1.15")
    implementation("io.javalin:javalin:6.3.0")
    implementation("io.javalin:javalin-rendering:6.3.0")
    implementation("io.javalin:javalin-bundle:6.3.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.jsoup:jsoup:1.17.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    implementation("com.konghq:unirest-java-core:4.4.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}