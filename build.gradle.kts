import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// The Beverage Buddy sample project ported to Kotlin.
// Original project: https://github.com/vaadin/beverage-starter-flow

plugins {
    kotlin("jvm") version "2.3.0"
    application
    alias(libs.plugins.vaadin)
}

defaultTasks("clean", "build")

repositories {
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        // to see the exception stacktraces of failed tests in the CI console
        exceptionFormat = TestExceptionFormat.FULL
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Vaadin
    implementation(libs.vaadin.core)
    if (!vaadin.effective.productionMode.get()) {
        implementation(libs.vaadin.dev)
    }
    implementation(libs.karibu.dsl)
    implementation(libs.vaadin.boot)

    implementation(libs.ktorm.vaadin)
    implementation(libs.hikaricp)

    // logging
    // currently we are logging through the SLF4J API to SLF4J-Simple. See src/main/resources/simplelogger.properties file for the logger configuration
    implementation(libs.slf4j.simple)

    // db
    implementation(libs.flyway) // newest version: https://repo1.maven.org/maven2/org/flywaydb/flyway-core/
    implementation(libs.h2) // remove this and replace it with a database driver of your choice.

    // REST
    api(libs.javalin) {
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation(libs.bundles.gson)

    // testing
    testImplementation(libs.karibu.testing)
    testImplementation(libs.junit)
    testImplementation(libs.bundles.ktor)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_21
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("com.vaadin.starter.beveragebuddy.MainKt")
}
