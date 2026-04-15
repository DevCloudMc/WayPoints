import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.3"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("com.diffplug.spotless") version "6.25.0"
    `maven-publish`
}

group = "org.devcloud"
version = "2.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.aaaaahhhhhhh.com/releases") // Cartographer2
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("io.github.bananapuncher714:cartographer2_api:2.15.11")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("org.xerial:sqlite-jdbc:3.46.1.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.133.2")
    testImplementation("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    testImplementation("io.github.bananapuncher714:cartographer2_api:2.15.11")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    relocate("org.sqlite", "org.devcloud.waypoints.shadow.sqlite")
    relocate("org.bstats", "org.devcloud.waypoints.shadow.bstats")
}

tasks.build { dependsOn(tasks.shadowJar) }

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$projectDir/detekt.yml")
    autoCorrect = false
}

spotless {
    kotlin {
        ktfmt("0.52").kotlinlangStyle()
        target("src/**/*.kt")
    }
    kotlinGradle {
        ktfmt("0.52").kotlinlangStyle()
        target("*.gradle.kts")
    }
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            artifact(tasks.named("shadowJar")) {
                classifier = ""
            }
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
                username = System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("gpr.user").orNull
                password = System.getenv("GITHUB_TOKEN") ?: providers.gradleProperty("gpr.key").orNull
            }
        }
    }
}
