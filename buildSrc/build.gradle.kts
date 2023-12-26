plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("versioning") {
            id = "dev.mieser.versioning"
            implementationClass = "dev.mieser.tsa.gradle.VersioningPlugin"
        }
    }
}