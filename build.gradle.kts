import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.4.21"
    id("com.gradleup.shadow") version "9.6.1"
    id("dev.detekt") version "2.0.0-alpha.6"
    id("com.diffplug.spotless") version "8.11.0"
    id("org.jetbrains.dokka") version "2.3.0"
    `maven-publish`
}

group = "org.devcloud"

version = "4.0.0-rc.1"

java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }

kotlin { jvmToolchain(25) }

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

// Cartographer2 API is vendored under libs/ because its upstream repo (repo.aaaaahhhhhhh.com) is
// down and it has no GitHub releases.
// Built with javac from the `api` module of github.com/BananaPuncher714/Cartographer2 at commit
// 1158310 (plugin version 2.15.18).
val cartographer2 = files("libs/cartographer2_api-2.15.18.jar")

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.253-stable")
    compileOnly(cartographer2)
    compileOnly("me.clip:placeholderapi:2.12.3")

    implementation("org.bstats:bstats-bukkit:3.2.1")

    // Paper ships sqlite-jdbc; a shaded and relocated copy cannot load its native library.
    testRuntimeOnly("org.xerial:sqlite-jdbc:3.54.1.0")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("io.mockk:mockk:1.14.12")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.120.0")
    // MockBukkit 4.120.0 is built against build 200 and its registry mocks fail on newer builds.
    testImplementation("io.papermc.paper:paper-api:26.3.build.200-stable")
    testImplementation(cartographer2)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("module.json") { expand("version" to project.version) }
}

tasks.test {
    useJUnitPlatform()
    testLogging { events("passed", "failed", "skipped") }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
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
        jdkVersion.set(25)
        reportUndocumented.set(false)
        skipEmptyPackages.set(true)
        // Document only the intentionally-public surface.
        perPackageOption {
            matchingRegex.set(
                "org\\.devcloud\\.waypoints\\.(service|storage|command|listener|provider|config|messaging|integration|util)(\\..*)?"
            )
            suppress.set(true)
        }
        externalDocumentationLinks.register("paper") { url("https://jd.papermc.io/paper/26.3/") }
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
