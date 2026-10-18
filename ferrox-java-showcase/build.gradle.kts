plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":ferrox-starter"))
    implementation(project(":ferrox-java-core"))
    implementation(project(":ferrox-java-security"))
    implementation(project(":ferrox-java-cqrs"))
    implementation(project(":ferrox-java-data"))
    implementation(project(":ferrox-java-offheap"))
    implementation(project(":ferrox-java-resilience"))
    implementation(project(":ferrox-java-observability"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.apache.kafka:kafka-clients")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
