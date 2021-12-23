plugins {
    `java-convention`
}

val webResource by configurations.creating

configurations.runtimeOnly {
    extendsFrom(webResource)
}

dependencies {
    implementation(project(":integration"))

    implementation("org.springframework:spring-webmvc")
    implementation("org.springframework:spring-tx")
    implementation("org.webjars:webjars-locator:0.42")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("commons-codec:commons-codec")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("commons-io:commons-io:2.11.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("jakarta.servlet:jakarta.servlet-api")

    integrationTestImplementation("org.hibernate.validator:hibernate-validator")
    integrationTestRuntimeOnly("org.glassfish:jakarta.el")

    webResource("org.webjars:bootstrap:5.1.3")
    webResource("org.webjars:jquery:3.6.0")
    webResource("org.webjars.npm:bootstrap-icons:1.7.0")
    webResource("org.webjars:datatables:1.11.3") {
        exclude("org.webjars", "jquery")
    }

}
