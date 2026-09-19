plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":ferrox-java-core"))
    implementation(project(":ferrox-java-security"))
    implementation(project(":ferrox-java-cqrs"))
    implementation(project(":ferrox-java-data"))
    implementation(project(":ferrox-java-web"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
}
