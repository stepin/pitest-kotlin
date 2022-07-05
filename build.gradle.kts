import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    kotlin("jvm") version "1.7.0"
    `java-library`
    `maven-publish`
    signing
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
//    id("io.gitlab.arturbosch.detekt") version "1.21.0-RC2"
    id("org.jetbrains.dokka") version "1.7.0"
    jacoco
    id("org.sonarqube") version "3.4.0.2513"
    id("pl.allegro.tech.build.axion-release") version "1.13.14"
}

group = "name.stepin"
version = scmVersion.version

repositories {
    mavenCentral()
}

val pitestVersion = "1.7.4"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.pitest:pitest:$pitestVersion")
    compileOnly("org.pitest:pitest-entry:$pitestVersion")
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.0")

    testImplementation("org.pitest:pitest:$pitestVersion")
    testImplementation("org.pitest:pitest-entry:$pitestVersion")
    testImplementation("org.pitest", "pitest", "$pitestVersion", classifier = "tests")
    testImplementation("org.pitest", "pitest-entry", "$pitestVersion", classifier = "tests")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.12.4")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Vendor" to project.group,
                "Implementation-Version" to project.version
            )
        )
    }
}

java {
    withSourcesJar()
}

val dokkaHtml by tasks.getting(DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar)
        pom {
            val projectGitUrl = "https://github.com/stepin/pitest-kotlin"
            name.set(rootProject.name)
            description.set("Improves pitest's support for Kotlin.")
            url.set(projectGitUrl)
            inceptionYear.set("2022")
            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://opensource.org/licenses/Apache-2.0")
                }
            }
            developers {
                developer {
                    id.set("stepin.name")
                    name.set("Igor Stepin")
                    email.set("igor_for_os@stepin.name")
                    url.set("https://stepin.name")
                }
            }
            issueManagement {
                system.set("GitHub")
                url.set("$projectGitUrl/issues")
            }
            scm {
                connection.set("scm:git:$projectGitUrl")
                developerConnection.set("scm:git:$projectGitUrl")
                url.set(projectGitUrl)
            }
        }
        the<SigningExtension>().sign(this)
    }
    repositories {
        maven {
            name = "sonatypeStaging"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials(PasswordCredentials::class)
        }
    }
}

signing {
    useGpgCmd()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set("Pitest Kotlin plugin")
            includes.from("Module.md")
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(
                    URL(
                        "https://github.com/stepin/pitest-kotlin/tree/main/src/main/kotlin"
                    )
                )
                remoteLineSuffix.set("#L")
            }
        }
    }
}
