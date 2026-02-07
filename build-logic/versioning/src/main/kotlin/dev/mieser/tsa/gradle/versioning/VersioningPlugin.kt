package dev.mieser.tsa.gradle.versioning

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.of

/**
 * Sets the project version based on the [GITHUB_REF](https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables)
 * environment variable or the current branch name, when the environment variable is not set.
 */
class VersioningPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val versionProvider = project.providers.of(VersionValueSource::class) {}
        project.version = versionProvider.get()
    }

}