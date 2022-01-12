rootProject.name = "tsa-server"

include(
        "domain",
        "signing",
        "datetime",
        "app",
        "web",
        "integration",
        "persistence",
        "test-util"
)

enableFeaturePreview("VERSION_CATALOGS")
