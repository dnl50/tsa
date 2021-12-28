import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    `java-convention`
    `spotless-config`
    id("org.springframework.boot") version "2.6.2"
}

dependencies {
    implementation(project(":web"))
    implementation(project(":signing"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.hibernate.validator:hibernate-validator")

    integrationTestImplementation("org.apache.commons:commons-lang3")
    integrationTestImplementation("io.rest-assured:rest-assured")
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    integrationTestImplementation("com.fasterxml.jackson.core:jackson-databind")

    developmentOnly("org.springframework.boot:spring-boot-devtools:2.6.2")
}

tasks.getByName<BootRun>("bootRun") {
    args = listOf("--spring.profiles.active=dev")
}
