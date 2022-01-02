plugins {
    `java-convention`
    `spotless-config`
}

dependencies {
    api(project(":domain"))
    api(libs.bouncycastle.bcpkix)

    implementation("commons-io:commons-io:2.11.0")
    implementation("org.mockito:mockito-core")

    testImplementation("org.apache.commons:commons-lang3")
}
