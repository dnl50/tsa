plugins {
    `java-convention`
    `spotless-config`
}

dependencies {
    api(project(":datetime"))
    api(project(":domain"))

    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation(libs.bouncycastle.bcpkix)
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("commons-io:commons-io:${libs.versions.commonsIo.get()}")
    implementation("org.apache.commons:commons-lang3")

    testImplementation(project(":test-util"))
    testImplementation("org.apache.commons:commons-lang3")
    testImplementation("commons-codec:commons-codec")

    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation("org.hibernate.validator:hibernate-validator")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
