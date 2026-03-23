import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

// Never read "GROUP": JitPack injects -Pgroup=com.github.jalagama and would corrupt .module identity.
val publishGroup =
    rootProject.findProperty("ALERTKIT_MAVEN_GROUP") as String? ?: "com.github.jalagama.AlertKit"
// Prefer JitPack's -Pversion=<git-tag> over VERSION_NAME from gradle.properties.
val publishVersion =
    (rootProject.findProperty("version") ?: rootProject.findProperty("VERSION_NAME")) as String?
        ?: "0.1.0-SNAPSHOT"

group = publishGroup
version = publishVersion

afterEvaluate {
    extensions.configure<PublishingExtension>("publishing") {
        if (publications.findByName("release") != null) return@configure
        publications.create<MavenPublication>("release") {
            groupId = publishGroup
            artifactId = project.name
            version = publishVersion
            from(components["release"])
            pom {
                name.set("${rootProject.name} ${project.name}")
                description.set("Priority-based in-app popup queue for Android (${project.name}).")
                url.set("https://github.com/jalagama/AlertKit")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/jalagama/AlertKit.git")
                    developerConnection.set("scm:git:ssh://git@github.com/jalagama/AlertKit.git")
                    url.set("https://github.com/jalagama/AlertKit")
                }
            }
        }
    }
}
