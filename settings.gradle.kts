rootProject.name = "tsa"

include("app")
include("web")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("quarkus", "3.17.2")
            version("mapstruct", "1.6.3")

            plugin("quarkus", "io.quarkus").versionRef("quarkus")
            plugin("lombok", "io.freefair.lombok").version("8.11")
            plugin("spotless", "com.diffplug.spotless").version("6.25.0")

            library("quarkus-bom", "io.quarkus", "quarkus-bom").versionRef("quarkus")
            library("bouncycastle", "org.bouncycastle", "bcpkix-jdk18on").version("1.79")
            library("mapstruct-processor", "org.mapstruct", "mapstruct-processor").versionRef("mapstruct")
            library("mapstruct-runtime", "org.mapstruct", "mapstruct").versionRef("mapstruct")
        }

        create("testLibs") {
            library("assertj", "org.assertj:assertj-core:3.26.3")
            library("archunit", "com.tngtech.archunit:archunit:1.3.0")
        }
    }
}