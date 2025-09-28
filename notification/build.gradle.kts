plugins {
    id("java-library")
    id("askme.spring-boot-service")
    kotlin("plugin.jpa")
}

group = "com.ruimendes"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(projects.common)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.spring.boot.starter.mail)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}