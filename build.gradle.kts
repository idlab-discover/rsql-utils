import java.net.URI

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.9.21"
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.github.nstdio:rsql-parser:2.2.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("org.apache.commons:commons-text:1.11.0")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    testImplementation("ch.qos.logback:logback-classic:1.4.14")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}

object Meta {
    const val desc = "Type-safe query builder and utilities for working with RSQL style queries in Kotlin and Java"
    const val license = "Apache 2.0"
    const val licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0.html"
    const val githubRepo = "idlab-discover/rsql-utils"
    const val release = "https://oss.sonatype.org/service/local/"
    const val snapshot = "https://oss.sonatype.org/content/repositories/snapshots/"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
//            artifact(tasks["sourcesJar"])
//            artifact(tasks["javadocJar"])

            pom {
                description.set(Meta.desc)
                url.set("https://github.com/${Meta.githubRepo}")

                licenses {
                    license {
                        name.set(Meta.license)
                        url.set(Meta.licenseUrl)
                    }
                }

                developers {
                    developer {
                        id.set("wkerckho")
                        name.set("Wannes Kerckhove")
                        organization.set("IDLab")
                        organizationUrl.set("https://www.ugent.be/ea/idlab/en")
                    }
                    developer {
                        id.set("jveessen")
                        name.set("Jasper Vaneessen")
                        organization.set("IDLab")
                        organizationUrl.set("https://www.ugent.be/ea/idlab/en")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "myRepo"
            url = uri(layout.buildDirectory.dir("repo"))
        }

        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/${Meta.githubRepo}")

            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

}
