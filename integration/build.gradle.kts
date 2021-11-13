plugins {
    `java-convention`
}

dependencies {
    api(project(":domain"))

    implementation("org.springframework:spring-context")

    implementation(project(":signing"))
    implementation(project(":persistence"))
}
