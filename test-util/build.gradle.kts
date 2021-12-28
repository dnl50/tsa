plugins {
    `java-convention`
    `spotless-config`
}

dependencies {
    api(project(":domain"))
    api("org.bouncycastle:bcpkix-jdk15on:1.70")

    implementation("commons-io:commons-io:2.11.0")
    implementation("org.mockito:mockito-core")

    testImplementation("org.apache.commons:commons-lang3")
}
