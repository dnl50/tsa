plugins {
    `java-convention`
}

dependencies {
    api(project(":integration"))

    implementation("org.springframework:spring-webmvc")
    implementation("org.springframework:spring-tx")
}
