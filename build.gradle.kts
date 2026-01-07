plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "GitHub REST API"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    implementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.apache.commons:commons-lang3:3.14.0")
    testImplementation("org.wiremock:wiremock-standalone:3.13.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
