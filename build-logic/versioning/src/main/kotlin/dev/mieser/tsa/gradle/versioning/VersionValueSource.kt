package dev.mieser.tsa.gradle.versioning

import dev.mieser.tsa.gradle.versioning.Ref.Type
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import javax.inject.Inject

abstract class VersionValueSource @Inject constructor(private val execOperations: ExecOperations) :
    ValueSource<String, ValueSourceParameters.None> {

    private val log = Logging.getLogger(VersionValueSource::class.java)

    private companion object {

        const val GITHUB_REF = "GITHUB_REF"

        const val GITHUB_REF_FORMAT = "^refs/(?<type>heads|pull|tags)/(?<value>.+)$"

        const val PULL_REQUEST_PREFIX = "PR"

        const val SNAPSHOT_SUFFIX = "SNAPSHOT"

    }

    override fun obtain(): String {
        return readRefFromEnvironment()?.run(::formatRef) ?: determineVersionFromCurrentEnv()
    }

    private fun determineVersionFromCurrentEnv(): String {
        log.info("Inferring project version from current git branch name...")

        val standardOut = ByteArrayOutputStream()
        try {
            execOperations.exec {
                commandLine("git", "symbolic-ref", "--short", "HEAD")
                standardOutput = standardOut
            }
        } catch (e: Exception) {
            log.info("Failed to execute git command! Is git installed?", e)
            return Project.DEFAULT_VERSION
        }

        val currentBranch = standardOut.toString(UTF_8).trim()
        log.info("Current branch: {}", currentBranch)
        return "${currentBranch.substringAfterLast('/')}-$SNAPSHOT_SUFFIX"
    }

    private fun readRefFromEnvironment(): Ref? {
        val ref = System.getenv()[GITHUB_REF]
        if (ref.isNullOrBlank()) {
            log.info("Environment variable '{}' is not set.", GITHUB_REF)
            return null
        }

        val matcher = GITHUB_REF_FORMAT.toPattern().matcher(ref)
        if (!matcher.matches()) {
            log.warn(
                "The value of environment variable '{}' ('{}') does not match the expected format!",
                GITHUB_REF,
                ref
            )
            return null
        }

        val type = Type.entries.find { it.identifier == matcher.group("type") }
            ?: error("unknown type '${matcher.group("type")}'")
        val value = matcher.group("value")

        return Ref(type, value)
    }

    private fun formatRef(ref: Ref): String {
        return when (ref.type) {
            Type.TAG -> ref.value

            Type.PULL_REQUEST -> {
                val pullRequestNumber = ref.value.substringBefore('/')
                "$PULL_REQUEST_PREFIX-$pullRequestNumber-$SNAPSHOT_SUFFIX"
            }

            Type.BRANCH -> "${ref.value.replace('/', '-')}-$SNAPSHOT_SUFFIX"
        }
    }

}