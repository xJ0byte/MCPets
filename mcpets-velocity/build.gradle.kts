plugins {
    id("com.gradleup.shadow")
}

description = "MCPets Velocity - proxyweiter Reload und Pet-Status ueber Octopus"

val velocityApiVersion = property("velocityApiVersion") as String
val octopusApiJar = property("octopusApiJar") as String
val chameleonApiJar = property("chameleonApiJar") as String

dependencies {
    implementation(project(":mcpets-api"))

    compileOnly("com.velocitypowered:velocity-api:$velocityApiVersion")
    annotationProcessor("com.velocitypowered:velocity-api:$velocityApiVersion")

    compileOnly(files("$rootDir/$octopusApiJar"))
    compileOnly(files("$rootDir/$chameleonApiJar"))
}

// Die @Plugin-Annotation von Velocity braucht eine Compile-Time-Konstante fuer die Version.
// Sie wird aus gradle.properties generiert, damit sie nicht doppelt gepflegt wird.
val generatedSources = layout.buildDirectory.dir("generated/sources/buildConstants/java")

val generateBuildConstants by tasks.registering {
    val version = project.version.toString()
    val outputDir = generatedSources
    inputs.property("version", version)
    outputs.dir(outputDir)

    doLast {
        val packageDir = outputDir.get().asFile.resolve("de/j0byte/mcpets/velocity")
        packageDir.mkdirs()
        packageDir.resolve("BuildConstants.java").writeText(
            """
            package de.j0byte.mcpets.velocity;

            /**
             * Generiert aus gradle.properties - nicht von Hand bearbeiten.
             */
            public final class BuildConstants {

                public static final String VERSION = "$version";

                private BuildConstants() {
                }
            }
            """.trimIndent() + "\n"
        )
    }
}

sourceSets.main {
    java.srcDir(generatedSources)
}

tasks.named("compileJava") {
    dependsOn(generateBuildConstants)
}

tasks.shadowJar {
    archiveBaseName.set("MCPets-Velocity")
    archiveClassifier.set("")

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/maven/**")
    mergeServiceFiles()
}

tasks.jar {
    archiveClassifier.set("thin")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
