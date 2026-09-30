plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":ferrox-starter"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

