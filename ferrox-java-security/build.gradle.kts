dependencies {
    api(project(":ferrox-java-core"))
    
    // Spring Security abstractions
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // PASETO Token support
    implementation("dev.paseto:jpaseto-api:0.7.0")
    runtimeOnly("dev.paseto:jpaseto-impl:0.7.0")
    runtimeOnly("dev.paseto:jpaseto-bouncy-castle:0.7.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.springframework.security:spring-security-crypto")
    
    // Redis for Sentinel Rate Limiting
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "dev.ferrox"
            version = "1.0.0"
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/YOUR_ORG/ferrox-java")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
