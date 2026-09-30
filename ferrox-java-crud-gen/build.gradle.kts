plugins {
    id("maven-publish")
    id("java-library")
}

dependencies {
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
}
