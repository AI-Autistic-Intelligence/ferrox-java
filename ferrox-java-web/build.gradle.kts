dependencies {
    api(project(":ferrox-java-core"))
    api(project(":ferrox-java-security"))
    api(project(":ferrox-java-cqrs"))
    api(project(":ferrox-java-data"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
}
