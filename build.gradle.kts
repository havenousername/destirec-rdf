plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.4"
}

group = "org.destirec"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.apache.commons:commons-lang3")


    implementation("org.eclipse.rdf4j:rdf4j-spring:+")
    implementation(platform("org.eclipse.rdf4j:rdf4j-bom:+"))
    implementation("org.eclipse.rdf4j:rdf4j-storage")
    implementation("org.eclipse.rdf4j:rdf4j-tools-federation:+")

    implementation("org.springframework.boot:spring-boot-devtools")

    // OWL API core
    implementation("net.sourceforge.owlapi:owlapi-distribution:5.5.0")
    implementation("net.sourceforge.owlapi:owlapi-rio:5.5.0")
    implementation("net.sourceforge.owlapi:org.semanticweb.hermit:1.4.5.519")
    implementation("net.sourceforge.owlapi:owlexplanation:+")

    // Libraries for safe internet protocol requests
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("io.github.resilience4j:resilience4j-retry:2.3.0")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.3.0")

//    implementation("org.drools:drools-core:6.5.0.Final")
//    implementation("org.drools:drools-compiler:6.5.0.Final")
//    implementation("org.drools:drools-osgi-integration:6.5.0.Final")

    runtimeOnly("com.h2database:h2:2.3.+")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.javatuples:javatuples:1.2")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.eclipse.jetty:jetty-client:12.0.16")
    implementation("org.eclipse.jetty:jetty-util:12.0.16")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
