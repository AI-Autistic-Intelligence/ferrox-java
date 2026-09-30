plugins {
    id("java-library")
    id("org.springframework.boot") version "3.3.0" apply false
    id("io.spring.dependency-management") version "1.1.5"
}

allprojects {
    group = "io.github.ai-autistic-intelligence"
    version = "1.0.0"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "maven-publish")

    if (project.name != "ferrox-dependencies") {
        apply(plugin = "java-library")
        apply(plugin = "io.spring.dependency-management")

        java {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
            withSourcesJar()
            withJavadocJar()
        }

        dependencyManagement {
            imports {
                mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.0")
            }
        }
        
        dependencies {
            compileOnly("org.projectlombok:lombok:1.18.32")
            annotationProcessor("org.projectlombok:lombok:1.18.32")
            testCompileOnly("org.projectlombok:lombok:1.18.32")
            testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
            
            testImplementation("org.springframework.boot:spring-boot-starter-test")
        }
        
        tasks.withType<Test> {
            useJUnitPlatform()
            jvmArgs("--enable-preview")
        }
        
        tasks.withType<JavaCompile> {
            options.compilerArgs.add("--enable-preview")
        }
        
        tasks.withType<Javadoc> {
            isFailOnError = false
            (options as StandardJavadocDocletOptions).apply {
                addBooleanOption("-enable-preview", true)
                addStringOption("source", "21")
            }
        }
        
        tasks.withType<GenerateModuleMetadata> {
            suppressedValidationErrors.add("dependencies-without-versions")
        }
        
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    pom {
                        name.set(project.name)
                        description.set("Ferrox Framework module: ${project.name}")
                        url.set("https://github.com/AI-Autistic-Intelligence/ferrox-java")
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        developers {
                            developer {
                                id.set("ai-autistic-intelligence")
                                name.set("AI Autistic Intelligence")
                                email.set("info@ferrox.dev")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/AI-Autistic-Intelligence/ferrox-java.git")
                            developerConnection.set("scm:git:ssh://github.com/AI-Autistic-Intelligence/ferrox-java.git")
                            url.set("https://github.com/AI-Autistic-Intelligence/ferrox-java")
                        }
                    }
                }
            }
        }
    }
}
