plugins {
    `application`
    `jacoco`
    alias(libs.plugins.springboot) apply true
    alias(libs.plugins.dependency.management) apply true
    alias(libs.plugins.shadow) apply true
    alias(libs.plugins.spotless) apply true
    alias(libs.plugins.sonarqube) apply true
    alias(libs.plugins.lombok) apply true
}

group = "org.argos.file.manager"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("org.argos.file.manager.ArgosFileManagerApplication")
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(libs.springboot.starter.web)
    compileOnly(libs.lombok)
    developmentOnly(libs.springboot.devtools)
    implementation(libs.dotenv.java)
    implementation(libs.aws.s3)
    annotationProcessor(libs.lombok)
    testImplementation(libs.springboot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}


tasks.test {
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        csv.required = false
        html.required = true
    }
}

spotless {
    java {
        googleJavaFormat("1.24.0").aosp()
            .reflowLongStrings()
            .formatJavadoc(false)
            .reorderImports(false)
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit.jupiter.get())
        }
    }
}

sonar {
    val sonarProjectKey = System.getenv("SONAR_PROJECT_KEY") ?: ""
    val sonarHostUrl = System.getenv("SONAR_HOST_URL") ?: ""
    val sonarToken = System.getenv("SONAR_TOKEN") ?: ""
    properties {
        property("sonar.projectKey", sonarProjectKey)
        property("sonar.host.url", sonarHostUrl)
        property("sonar.token", sonarToken)
        property("sonar.qualitygate.wait", "true")
    }
}
