plugins {
    id("com.gradleup.shadow")
}

description = "MCPets Paper - Pet-Plugin auf Basis von BetterModel, Shark, Octopus und Chameleon"

val relocationBase = property("relocationBase") as String
val paperApiVersion = property("paperApiVersion") as String
val paperApiTarget = property("paperApiTarget") as String
val commandApiVersion = property("commandApiVersion") as String
val betterModelVersion = property("betterModelVersion") as String
val craftEngineVersion = property("craftEngineVersion") as String
val guiceVersion = property("guiceVersion") as String

val sharkApiJar = property("sharkApiJar") as String
val sharkGuiJar = property("sharkGuiJar") as String
val octopusApiJar = property("octopusApiJar") as String
val chameleonApiJar = property("chameleonApiJar") as String

dependencies {
    // Einziges Modul, das wirklich mitgeshadet wird.
    implementation(project(":mcpets-api"))

    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")

    // Alle vier Frameworks laufen als eigene Plugins auf dem Server (join-classpath),
    // deshalb ausnahmslos compileOnly - nichts davon wird gebuendelt.
    compileOnly(files("$rootDir/$sharkApiJar"))
    compileOnly(files("$rootDir/$sharkGuiJar"))
    compileOnly(files("$rootDir/$octopusApiJar"))
    compileOnly(files("$rootDir/$chameleonApiJar"))

    // Guice kommt zur Laufzeit aus ByteOctopus. Eine zweite Kopie im eigenen Jar
    // fuehrt zu ArrayStoreException beim Bau des Injectors -> niemals implementation.
    compileOnly("com.google.inject:guice:$guiceVersion")

    // CommandAPI laeuft als eigenes Plugin.
    compileOnly("dev.jorel:commandapi-bukkit-core:$commandApiVersion")

    // BetterModel laeuft als eigenes Plugin.
    compileOnly("io.github.toxicity188:bettermodel-api:$betterModelVersion")
    compileOnly("io.github.toxicity188:bettermodel-bukkit-api:$betterModelVersion")

    // CraftEngine ist optional: ohne installiertes CraftEngine laeuft MCPets ganz normal
    // weiter, nur "namespace:id"-Items werden dann nicht aufgeloest.
    compileOnly("net.momirealms:craft-engine-bukkit:$craftEngineVersion")
    compileOnly("net.momirealms:craft-engine-core:$craftEngineVersion")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version.toString(),
        "apiVersion" to paperApiTarget
    )
    inputs.properties(props)
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveBaseName.set("MCPets-Paper")
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
