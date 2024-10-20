plugins {
    java
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.3"
}

group = "org.devcloud"
version = "2.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven ("https://jitpack.io")
    maven("https://repo.aaaaahhhhhhh.com/releases")

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")

}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("io.github.bananapuncher714:cartographer2_api:2.15.11")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.17.0")
}

tasks.shadowJar {
    archiveFileName.set("${project.name}-${project.version}.jar")
}