package dev.mieser.tsa.gradle.versioning

data class Ref(
    val type: Type,
    val value: String
) {

    enum class Type(val identifier: String) {

        BRANCH("heads"),

        PULL_REQUEST("pull"),

        TAG("tags")

    }

}
