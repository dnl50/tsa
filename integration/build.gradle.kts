plugins {
    `java-convention`
}

dependencies {
    api(project(":domain"))
    api(project(":signing"))

    implementation("org.springframework:spring-context")

    implementation(project(":persistence"))
}
