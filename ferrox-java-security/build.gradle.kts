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
