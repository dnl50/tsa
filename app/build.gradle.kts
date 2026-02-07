plugins {
    java
    alias(libs.plugins.quarkus)
    alias(libs.plugins.spotless)
    alias(libs.plugins.lombok)
    id("dev.mieser.versioning")
}

repositories {
    mavenCentral()
}

val openApiSpecification by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))

    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-websockets")

    implementation("commons-io:commons-io")
    implementation("org.apache.commons:commons-lang3")

    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("org.bouncycastle:bcpkix-jdk18on")
    implementation(libs.mapstruct.runtime)

    runtimeOnly("io.quarkus:quarkus-jdbc-h2")
    runtimeOnly("io.quarkus:quarkus-flyway")
    runtimeOnly("io.quarkus:quarkus-rest-jackson")
    runtimeOnly("io.quarkus:quarkus-container-image-docker")

    annotationProcessor(libs.mapstruct.processor)

    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation(libs.assertj)
    testImplementation(libs.archunit)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<Test>().configureEach {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

val compileAll by tasks.registering {
    description = "Lifecycle Task to compile all source sets"
    dependsOn(tasks.withType<JavaCompile>())
}

// this file is used in the publish-workflow as well
val openApiSpecificationFile = layout.buildDirectory.file("openapi-specification.json")

tasks.test {
    inputs.property("project-version", provider { project.version })
    outputs.file(openApiSpecificationFile)
    systemProperty("openapi.specification.target-file", openApiSpecificationFile.get().asFile.absolutePath)
}

artifacts {
    add(openApiSpecification.name, openApiSpecificationFile) {
        builtBy(tasks.test)
    }
}

spotless {
    java {
        importOrderFile("$rootDir/spotless.importorder")
        removeUnusedImports()
        eclipse().configFile("$rootDir/eclipse-formatter.xml")
    }
}

// setting "quarkus.native.enabled" is not enough when using Gradle, you also have to disable jar packaging
// see https://github.com/quarkusio/quarkus/discussions/40679
val buildNativeImage = providers.gradleProperty("nativeImage")
    .map { it.toBoolean() }
    .orElse(false)

quarkus {
    set("package.jar.enabled", buildNativeImage.map { !it }.map(Boolean::toString))
    set("native.enabled", buildNativeImage.map(Boolean::toString))
    set("native.container-build", "true")
    set("container-image.build", "true")
    set("container-image.group", "dnl50")
    set("container-image.name", "tsa-server")
    set(
        "container-image.tag",
        buildNativeImage.map { native ->
            "${project.version}${if (native) "" else "-jvm"}"
        }
    )
    finalName.set("tsa-${project.version}")
}

// for some reason quarkus does recognize that the JDBC URL is set in the prod profile. therefore it
// creates a h2 db in server mode (using the H2DevServicesProcessor) which runs on the host machine and sets the JDBC URL
// to something like "jdbc:h2:tcp://localhost:53233/mem:test" which obviously does not work inside the docker container
tasks.testNative {
    systemProperty("quarkus.datasource.jdbc.url", "jdbc:h2:file:/work/data/tsa")
}