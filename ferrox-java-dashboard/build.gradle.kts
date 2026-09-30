dependencies {
    implementation(project(":ferrox-java-core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
