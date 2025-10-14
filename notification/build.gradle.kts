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
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)

    runtimeOnly(libs.postgresql)

    implementation(libs.firebase.admin.sdk)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}