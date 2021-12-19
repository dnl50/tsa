plugins {
    `java-convention`
}

dependencies {
    api(project(":datetime"))
    api(project(":domain"))

    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.apache.commons:commons-lang3")

    testImplementation(project(":test-util"))

    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation("org.hibernate.validator:hibernate-validator")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
