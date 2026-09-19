pluginManagement {
    val shadowVersion: String by settings
    val foojayVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("com.gradleup.shadow") version shadowVersion
        id("org.gradle.toolchains.foojay-resolver-convention") version foojayVersion
    }
}

// Laedt das JDK der in gradle.properties gesetzten javaVersion automatisch nach,
// falls es lokal nicht installiert ist.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc"
        }
        maven("https://repo.extendedclip.com/releases/") {
            name = "placeholderapi"
        }
        maven("https://repo.momirealms.net/releases/") {
            name = "momirealms"
        }
    }
}

rootProject.name = "MCPets"

include("mcpets-api")
include("mcpets-paper")
include("mcpets-velocity")
