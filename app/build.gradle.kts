import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("askme.spring-boot-app")
}

group = "com.ruimendes"
version = "0.0.1-SNAPSHOT"
description = "Ask Me backend"

tasks {
    // Make sure resources from other resoruces are included in the final jar
    named<BootJar>("bootJar") {
        from(project(":notification").projectDir.resolve("src/main/resources")) {
            into("")
        }
        from(project(":user").projectDir.resolve("src/main/resources")) {
            into("")
        }
    }
}

dependencies {
    implementation(projects.chat)
    implementation(projects.user)
    implementation(projects.notification)
    implementation(projects.common)

    implementation(libs.spring.boot.starter.security)
    implementation(libs.jackson.datatype.jsr310)

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.mail)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.security.test)
    testImplementation(libs.mockito.kotlin)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
