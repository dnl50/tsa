plugins {
    `java-convention`
    `spotless-config`
}

dependencies {
    api(project(":domain"))
    api(libs.bouncycastle.bcpkix)

    implementation("commons-io:commons-io:${libs.versions.commonsIo.get()}")
    implementation("org.mockito:mockito-core")

    testImplementation("org.apache.commons:commons-lang3")
}
