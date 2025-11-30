plugins {
    id("idea")
    id("java")
    id("java-library")
    id("com.github.spotbugs") version "6.4.6"
}

group = "com.smalser"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

val spotbugsVersion = "4.9.8"

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // compile against SpotBugs API only
    compileOnly("com.github.spotbugs:spotbugs:$spotbugsVersion")
    compileOnly("com.github.spotbugs:spotbugs-annotations:$spotbugsVersion")

    // helpful at test/runtime inside this module
    testImplementation("com.github.spotbugs:spotbugs:$spotbugsVersion")


    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.github.spotbugs:test-harness-jupiter:$spotbugsVersion")

    // Force platform alignment
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:1.10.0")

    spotbugsPlugins(files(layout.buildDirectory.file("libs/SpotBugsHashCollectionsDetector-1.0-SNAPSHOT.jar")))
}

spotbugs {
    toolVersion.set(spotbugsVersion)
    ignoreFailures.set(false)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.HIGH)
}

tasks.spotbugsMain {
    reports {
        create("html") {
            required.set(true)
            outputLocation.set(layout.buildDirectory.file("reports/spotbugs/main.html"))
        }
    }
}
tasks.spotbugsTest {
    reports {
        create("html") {
            required.set(true)
            outputLocation.set(layout.buildDirectory.file("reports/spotbugs/test.html"))
        }
    }
}


tasks.test {
    dependsOn(tasks.classes)
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-parameters", "-g"))
}