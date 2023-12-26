plugins {
    java
    id("io.quarkus") version libs.versions.quarkus
    id("com.diffplug.spotless") version libs.versions.spotless
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
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:${libs.versions.quarkus.get()}"))

    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-websockets")

    implementation("commons-io:commons-io")
    implementation("org.apache.commons:commons-lang3")

    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("org.bouncycastle:bcpkix-jdk18on:${libs.versions.bouncycastle.get()}")
    implementation("org.mapstruct:mapstruct:${libs.versions.mapstruct.get()}")

    runtimeOnly("io.quarkus:quarkus-jdbc-h2")
    runtimeOnly("io.quarkus:quarkus-flyway")
    runtimeOnly("io.quarkus:quarkus-resteasy-reactive")
    runtimeOnly("io.quarkus:quarkus-resteasy-reactive-jackson")
    runtimeOnly("io.quarkus:quarkus-container-image-docker")

    compileOnly("org.projectlombok:lombok:${libs.versions.lombok.get()}")

    annotationProcessor("org.projectlombok:lombok:${libs.versions.lombok.get()}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${libs.versions.mapstruct.get()}")

    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.assertj:assertj-core:${libs.versions.assertj.get()}")
    testImplementation("com.tngtech.archunit:archunit:${libs.versions.archunit.get()}")

    testCompileOnly("org.projectlombok:lombok:${libs.versions.lombok.get()}")
    testAnnotationProcessor("org.projectlombok:lombok:${libs.versions.lombok.get()}")
}

if (hasProperty("projectVersion")) {
    version = property("projectVersion")!!
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test>().configureEach {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

val openApiSpecificationFile = layout.buildDirectory.file("openapi-specification.json")

tasks.test {
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

quarkus {
    set("native.container-build", "true")
    set("container-image.build", "true")
    set("container-image.group", "dnl50")
    set("container-image.name", "tsa-server")
    set("package.type", "native")
    finalName.set("tsa-${project.version}")
}

// TODO: for some reason quarkus does recognize that the JDBC URL is set in the prod profile. therefore it
//  creates a h2 db in server mode (using the H2DevServicesProcessor) which runs on the host machine and sets the JDBC URL
//  to something like "jdbc:h2:tcp://localhost:53233/mem:test" which obviously does not work inside the docker container
tasks.quarkusIntTest {
    systemProperty("quarkus.datasource.jdbc.url", "jdbc:h2:file:/work/data/tsa")
}