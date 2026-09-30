dependencies {
    api(project(":ferrox-java-core"))
    
    // We can use Spring context for the EventBus logic
    implementation("org.springframework:spring-context")
}
