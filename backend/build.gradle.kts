import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.owasp.dependencycheck") version "10.0.4"
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
    id("org.sonarqube") version "4.4.1.3373"
}

group = "hu.bme.sch"
version = "4.8.1"
java.sourceCompatibility = JavaVersion.VERSION_21

springBoot {
    buildInfo()
}

tasks {
    bootJar {
        archiveFileName.set("cmsch.jar")
    }
}

sonar {
    properties {
        property("sonar.projectKey", "kir-dev_cmsch-backend")
        property("sonar.organization", "kir-dev")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.firebase:firebase-admin:9.3.0")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    api("org.springframework.boot:spring-boot-configuration-processor")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.session:spring-session-jdbc")
    api("org.springframework.boot:spring-boot-starter-oauth2-client")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-thymeleaf")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.retry:spring-retry")
    api("org.springframework.boot:spring-boot-starter-aop")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.itextpdf:itext-core:8.0.5")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    api("com.google.zxing:core:3.5.3")
    api("com.google.zxing:javase:3.5.3")
    api("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    api("com.fasterxml.uuid:java-uuid-generator:5.1.0")
    api("org.commonmark:commonmark:0.22.0")
    api("org.commonmark:commonmark-ext-gfm-tables:0.22.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("com.h2database:h2")
    implementation("org.postgresql:postgresql")
    implementation(platform("io.micrometer:micrometer-bom:1.13.4"))
    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-observation")
    testApi("org.springframework.boot:spring-boot-starter-test")
    testApi("org.springframework.security:spring-security-test")
}

dependencyCheck {
    failBuildOnCVSS = 7f
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    buildpacks = listOf("gcr.io/paketo-buildpacks/adoptium", "urn:cnb:builder:paketo-buildpacks/java")
    builder = "paketobuildpacks/builder-jammy-base"
    environment = mapOf(
        "BP_NATIVE_IMAGE" to "false",
        "CDS_TRAINING_JAVA_TOOL_OPTIONS" to "-Dspring.profiles.include=prewarm",
        "BP_JVM_CDS_ENABLED" to "true",
        "BPL_JVM_CDS_ENABLED" to "true",
        "BPL_JVM_THREAD_COUNT" to "25"
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}
