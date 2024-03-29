plugins {
    base
}

val openApiSpecification by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    openApiSpecification(project(mapOf(
            "path" to ":app",
            "configuration" to "openApiSpecification")
    ))
}

val copyOpenApiSpec by tasks.registering(Copy::class) {
    from(openApiSpecification)
    into(layout.buildDirectory)
}