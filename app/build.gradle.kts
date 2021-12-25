plugins {
    `java-convention`
    id("org.springframework.boot") version "2.5.6"
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
}
