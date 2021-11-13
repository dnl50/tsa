plugins {
    `java-convention`
}

dependencies {
    implementation(project(":web"))
    implementation(project(":signing"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")
    implementation("org.hibernate.validator:hibernate-validator")
}
