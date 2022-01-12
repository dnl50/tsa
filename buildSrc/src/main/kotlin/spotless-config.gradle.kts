plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        importOrderFile("$rootDir/spotless.importorder")
        removeUnusedImports()
        eclipse().configFile("$rootDir/eclipse-formatter.xml")
    }
}
