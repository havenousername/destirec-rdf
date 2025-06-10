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

configurations.all {
    resolutionStrategy {
        force("org.eclipse.jetty:jetty-client:9.4.41.v20210516")
        force("org.eclipse.jetty:jetty-util:9.4.41.v20210516")
        force("org.eclipse.jetty:jetty-http:9.4.41.v20210516")
        force("org.eclipse.jetty.http2:http2-client:9.4.41.v20210516")
    }
}

configurations.all {
    exclude(group = "org.eclipse.jetty", module = "jetty-client")
    exclude(group = "org.eclipse.jetty", module = "jetty-http")
    exclude(group = "org.eclipse.jetty.http2", module = "http2-client")
    exclude(group = "org.eclipse.jetty.http2", module = "http2-hpack")
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


    implementation(platform("org.eclipse.rdf4j:rdf4j-bom:5.1.3"))
    implementation("org.eclipse.rdf4j:rdf4j-spring:5.1.3")
    implementation("org.eclipse.rdf4j:rdf4j-storage:5.1.3")
    implementation("org.eclipse.rdf4j:rdf4j-tools-federation:4.1.0")


    implementation("org.springframework.boot:spring-boot-devtools")

    val jettyVersion = "9.4.54.v20240208"
    implementation("org.eclipse.jetty:jetty-client:${jettyVersion}")
    implementation("org.eclipse.jetty:jetty-util:${jettyVersion}")
    implementation("org.eclipse.jetty:jetty-http:${jettyVersion}")
    implementation("org.eclipse.jetty.http2:http2-client:${jettyVersion}")
    implementation("org.eclipse.jetty.http2:http2-hpack:${jettyVersion}")
    implementation("org.eclipse.jetty:jetty-io:${jettyVersion}")



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
    // javascript runtime
    implementation("org.graalvm.polyglot:polyglot:24.2.1")
    implementation("org.graalvm.polyglot:js:24.2.1")


    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    // https://mvnrepository.com/artifact/org.apache.commons/commons-math3
    implementation("org.apache.commons:commons-math3:3.6.1")

    implementation("io.micrometer:micrometer-core:1.12.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
