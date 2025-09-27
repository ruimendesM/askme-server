plugins {
    id("java-library")
    id("askme.kotlin-common")
}

group = "com.ruimendes"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kotlin.reflect)
    api(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)

    implementation(libs.spring.boot.starter.amqp)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}