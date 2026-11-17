import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.4.21"
    id("com.gradleup.shadow") version "9.6.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("com.diffplug.spotless") version "8.11.0"
    id("org.jetbrains.dokka") version "2.3.0"
    `maven-publish`
}

group = "org.devcloud"

version = "3.0.0"

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

kotlin { jvmToolchain(21) }

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

// Cartographer2 API is vendored under libs/ because its upstream repo
// (repo.aaaaahhhhhhh.com) is unreliable and its JitPack coordinates don't resolve.
val cartographer2 = files("libs/cartographer2_api-2.15.11.jar")

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly(cartographer2)
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("org.xerial:sqlite-jdbc:3.46.1.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.133.2")
    testImplementation("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    testImplementation(cartographer2)
}

tasks.test {
    useJUnitPlatform()
    testLogging { events("passed", "failed", "skipped") }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    relocate("org.sqlite", "org.devcloud.waypoints.shadow.sqlite")
    relocate("org.bstats", "org.devcloud.waypoints.shadow.bstats")
}

tasks.build { dependsOn(tasks.shadowJar) }

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
}

dokka {
    dokkaPublications.html {
        moduleName.set("WayPoints")
        includes.from("docs/module.md")
    }
    dokkaSourceSets.configureEach {
        jdkVersion.set(21)
        reportUndocumented.set(false)
        skipEmptyPackages.set(true)
        // Document only the intentionally-public surface.
        perPackageOption {
            matchingRegex.set(
                "org\\.devcloud\\.waypoints\\.(service|storage|command|listener|provider|config|messaging|integration|util)(\\..*)?"
            )
            suppress.set(true)
        }
        externalDocumentationLinks.register("paper") { url("https://jd.papermc.io/paper/1.21.1/") }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$projectDir/detekt.yml")
    autoCorrect = true
}

spotless {
    kotlin {
        ktfmt("0.64").kotlinlangStyle()
        target("src/**/*.kt")
    }
    kotlinGradle {
        ktfmt("0.64").kotlinlangStyle()
        target("*.gradle.kts")
    }
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            artifact(tasks.named("shadowJar")) { classifier = "" }
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
            groupId = project.group.toString()
            artifactId = "waypoints"
            version = project.version.toString()
            pom {
                name.set("WayPoints")
                description.set("Waypoint addon for Cartographer2")
                url.set("https://github.com/DevCloudMc/WayPoints")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/DevCloudMc/WayPoints")
            credentials {
                username =
                    System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("gpr.user").orNull
                password =
                    System.getenv("GITHUB_TOKEN") ?: providers.gradleProperty("gpr.key").orNull
            }
        }
    }
}
