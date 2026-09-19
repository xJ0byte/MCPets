plugins {
    java
}

val javaVersion = (property("javaVersion") as String).toInt()

allprojects {
    group = rootProject.group
    version = rootProject.version
}

subprojects {
    apply(plugin = "java-library")

    val lombokVersion = property("lombokVersion") as String
    val annotationsVersion = property("annotationsVersion") as String
    val guavaVersion = property("guavaVersion") as String

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }

    dependencies {
        // Lombok muss in JEDEM Modul deklariert werden, es vererbt sich nicht.
        "compileOnly"("org.projectlombok:lombok:$lombokVersion")
        "annotationProcessor"("org.projectlombok:lombok:$lombokVersion")

        // JetBrains Annotations
        "compileOnly"("org.jetbrains:annotations:$annotationsVersion")

        // Guava wird von Paper und Velocity zur Laufzeit bereitgestellt.
        "compileOnly"("com.google.guava:guava:$guavaVersion")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(javaVersion)
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions)
            .addStringOption("Xdoclint:all,-missing", "-quiet")
    }

    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = "UTF-8"
    }
}
