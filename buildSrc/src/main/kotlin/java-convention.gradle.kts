import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`
    jacoco
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

val integrationTestImplementation by configurations.creating
val integrationTestRuntimeOnly by configurations.creating

integrationTestImplementation.extendsFrom(configurations.testImplementation.get())
integrationTestRuntimeOnly.extendsFrom(configurations.testRuntimeOnly.get())

// Workaround for using version catalogs in precompiled script plugins.
// See https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val libs = the<LibrariesForLibs>()

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"))
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"))

    implementation("org.slf4j:slf4j-api")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.mockito:mockito-junit-jupiter")
}

val integrationTestSourceSet = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val integrationTest by tasks.registering(Test::class) {
    group = "verification"

    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    shouldRunAfter(tasks.test)
}

tasks.withType(Test::class).configureEach {
    useJUnitPlatform()
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
}

tasks.check {
    dependsOn(integrationTest)
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

// Makes the project version configurable via a project property that can be passed in via
// the -P CLI parameter. Useful for CI builds to infer the project version from the branch name.
if (hasProperty("projectVersion")) {
    version = property("projectVersion")!!
}

tasks.withType(Jar::class).configureEach {
    manifest {
        attributes("Implementation-Version" to archiveVersion)
    }
}
