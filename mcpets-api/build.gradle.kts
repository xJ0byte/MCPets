description = "MCPets API - plattformneutrale Modelle, Dokumente und Sync-Nachrichten"

val octopusApiJar = property("octopusApiJar") as String

dependencies {
    // Octopus ist eine Shared Library ohne eigenen Server-Prozess, stellt hier aber nur
    // die Mongo-Annotationen. Zur Laufzeit kommt es aus ByteOctopus -> compileOnly.
    compileOnly(files("$rootDir/$octopusApiJar"))
}
