plugins {
    id("java-platform")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        // internal modules
        api(project(":ferrox-java-core"))
        api(project(":ferrox-java-web"))
        api(project(":ferrox-java-data"))
        api(project(":ferrox-java-dashboard"))
        api(project(":ferrox-java-utils"))
        api(project(":ferrox-java-security"))
        api(project(":ferrox-java-event-manager"))
        api(project(":ferrox-java-cqrs"))
        
        // external dependencies that are specific to ferrox could be placed here
    }
}

