plugins {
    `java-convention`
    `spotless-config`
}

val provided by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

configurations {
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

    runtimeOnly("org.thymeleaf:thymeleaf-spring6")

    testImplementation("commons-io:commons-io:${libs.versions.commonsIo.get()}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jsoup:jsoup:${libs.versions.jsoup.get()}")

    testRuntimeOnly("org.hibernate.validator:hibernate-validator")
    testRuntimeOnly("org.apache.tomcat.embed:tomcat-embed-el")

    runtimeOnly("org.webjars:bootstrap:${libs.versions.webjars.bootstrap.get()}")
    runtimeOnly("org.webjars.npm:bootstrap-icons:${libs.versions.webjars.bootstrapIcons.get()}")
    runtimeOnly("org.webjars:jquery:${libs.versions.webjars.jquery.get()}")
    runtimeOnly("org.webjars:popper.js:${libs.versions.webjars.poppers.get()}")
    runtimeOnly("org.webjars:datatables:${libs.versions.webjars.datatables.get()}") {
        exclude("org.webjars", "jquery")
    }

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.processResources.configure {
    // Copies each english properties resource bundle file into a file with the resource bundle's default name
    from("src/main/resources/") {
        include("*_en.properties")
        rename("(.*)_en.properties", "$1.properties")
    }
}
