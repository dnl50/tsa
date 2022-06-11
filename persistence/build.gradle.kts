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
    implementation("org.mapstruct:mapstruct:${libs.versions.mapstruct.get()}")
    implementation("commons-codec:commons-codec")
    implementation("org.flywaydb:flyway-core")

    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")

    annotationProcessor("org.mapstruct:mapstruct-processor:${libs.versions.mapstruct.get()}")

    runtimeOnly("com.h2database:h2")
}
