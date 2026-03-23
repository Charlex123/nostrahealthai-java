plugins {
    `java-library`
    `maven-publish`
}

group = "com.nostrahealthai"
version = "2.0.0"
description = "Official Java SDK for NostraHealthAI Medical AI Platform - Skin Analysis, Eye Diagnosis, Wound Tracking, Drug Verification, FHIR, and Subscriptions"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("NostraHealthAI SDK")
                description.set(project.description)
                url.set("https://docs.nostrahealthai.com/sdk")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        name.set("Nostra Health")
                        email.set("support@nostrahealth.com")
                        organization.set("Nostra Health")
                    }
                }
            }
        }
    }
}
