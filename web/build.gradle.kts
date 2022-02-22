plugins {
    `java-convention`
    `spotless-config`
}

val webResource by configurations.creating
val provided by configurations.creating

configurations {
    runtimeOnly {
        extendsFrom(webResource)
    }

    listOf(compileOnly, testImplementation).forEach {
        it.get().extendsFrom(provided)
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
    implementation("org.webjars:webjars-locator:0.42")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("commons-codec:commons-codec")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.apache.commons:commons-lang3")

    testImplementation("commons-io:commons-io:2.11.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    integrationTestImplementation("org.hibernate.validator:hibernate-validator")
    integrationTestRuntimeOnly("org.glassfish:jakarta.el")

    webResource("org.webjars:bootstrap:5.1.3")
    webResource("org.webjars:jquery:3.6.0")
    webResource("org.webjars.npm:bootstrap-icons:1.7.0")
    webResource("org.webjars:datatables:1.11.3") {
        exclude("org.webjars", "jquery")
    }

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

}

// Copies each english properties resource bundle file into a file with the resource bundle's default name
tasks.processResources {
    from("src/main/resources/") {
        include("*_en.properties")
        rename("(.*)_en.properties", "$1.properties")
    }
}
