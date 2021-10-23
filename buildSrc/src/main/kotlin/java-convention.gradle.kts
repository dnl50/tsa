plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val integrationTestImplementation by configurations.creating
val integrationTestRuntimeOnly by configurations.creating

integrationTestImplementation.extendsFrom(configurations.testImplementation.get())
integrationTestRuntimeOnly.extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.5.6"))
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:2.5.6"))

    implementation("org.slf4j:slf4j-api")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.mockito:mockito-junit-jupiter")
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
