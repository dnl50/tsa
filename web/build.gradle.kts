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
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    testImplementation("commons-io:commons-io:2.11.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("jakarta.servlet:jakarta.servlet-api")

    webResource("org.webjars:bootstrap:5.1.3")
    webResource("org.webjars:jquery:3.6.0")
    webResource("org.webjars.npm:bootstrap-icons:1.7.0")
}
