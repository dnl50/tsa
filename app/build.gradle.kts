import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    `java-convention`
    `spotless-config`
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.gitProperties)
}

dependencies {
    implementation(project(":web"))
    implementation(project(":signing"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")

    integrationTestImplementation("org.apache.commons:commons-lang3")
    integrationTestImplementation("io.rest-assured:rest-assured")
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation(libs.bouncycastle.bcpkix)
    integrationTestImplementation("com.fasterxml.jackson.core:jackson-databind")

    runtimeOnly("org.hibernate.validator:hibernate-validator")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    developmentOnly("org.springframework.boot:spring-boot-devtools:${libs.versions.spring.boot.get()}")
}

tasks.getByName<BootRun>("bootRun") {
    args = listOf("--spring.profiles.active=dev")
}

springBoot {
    buildInfo {
        properties {
            excludes.set(setOf("time", "artifact", "group", "name"))
        }
    }
}
