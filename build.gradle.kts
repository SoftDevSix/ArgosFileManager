plugins {
    application
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.sonarqube") version "5.1.0.4882"
    id("jacoco")
}

group = "org.argos"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

sonarqube {
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

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.argos.file.manager.ArgosFileManagerApplication")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.8".toBigDecimal()
            }
        }
    }
}
