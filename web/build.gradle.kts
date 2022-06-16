plugins {
    `java-convention`
    `spotless-config`
}

val webResource by configurations.creating
val provided by configurations.creating
listOf(webResource, provided).forEach { config ->
    config.isCanBeResolved = true
    config.isCanBeConsumed = false
}

configurations {
    runtimeOnly {
        extendsFrom(webResource)
    }

    listOf(compileOnly, testImplementation).forEach { config ->
        config.configure {
            extendsFrom(provided)
        }
    }
}

dependencies {
    provided("org.apache.tomcat:tomcat-catalina:${libs.versions.tomcat.get()}") {
        exclude("org.apache.tomcat", "tomcat-servlet-api")
        exclude("org.apache.tomcat", "tomcat-annotations-api")
    }
    provided("jakarta.annotation:jakarta.annotation-api")

    implementation(project(":integration"))
    implementation("org.springframework:spring-webmvc")
    implementation("org.springframework:spring-tx")
    implementation("org.webjars:webjars-locator:${libs.versions.webjars.locator.get()}")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("commons-codec:commons-codec")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.apache.commons:commons-lang3")
    implementation("commons-io:commons-io:${libs.versions.commonsIo.get()}")

    testImplementation("commons-io:commons-io:${libs.versions.commonsIo.get()}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    integrationTestImplementation("org.hibernate.validator:hibernate-validator")
    integrationTestRuntimeOnly("org.glassfish:jakarta.el")

    webResource("org.webjars:bootstrap:${libs.versions.webjars.bootstrap.get()}")
    webResource("org.webjars.npm:bootstrap-icons:${libs.versions.webjars.bootstrapIcons.get()}")
    webResource("org.webjars:jquery:${libs.versions.webjars.jquery.get()}")
    webResource("org.webjars:datatables:${libs.versions.webjars.datatables.get()}") {
        exclude("org.webjars", "jquery")
    }

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

val generateApplicationVersionFile by tasks.registering {
    val outputFile = file("$buildDir/application-version.txt")

    inputs.property("version", version)
    outputs.file(outputFile)

    doLast {
        outputFile.writeText(version.toString())
    }
}


tasks.processResources.configure {
    // Copies each english properties resource bundle file into a file with the resource bundle's default name
    from("src/main/resources/") {
        include("*_en.properties")
        rename("(.*)_en.properties", "$1.properties")
    }

    // this file is used to display the application version in the page footer
    from(generateApplicationVersionFile) {
        into("META-INF")
    }
}
