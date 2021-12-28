plugins {
    `java-convention`
    `spotless-config`
}

dependencies {
    api(project(":domain"))
    api(project(":signing"))
    api("org.springframework.data:spring-data-commons")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.mapstruct:mapstruct:1.4.2.Final")
    implementation("commons-codec:commons-codec")
    implementation("org.flywaydb:flyway-core")

    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")

    annotationProcessor("org.mapstruct:mapstruct-processor:1.4.2.Final")

    runtimeOnly("com.h2database:h2")
}
