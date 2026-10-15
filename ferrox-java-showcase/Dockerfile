# Multi-Stage Dockerfile for Ferrox Java Enterprise Showcase
# Stage 1: Build application with Gradle and Java 21
FROM gradle:8.8-jdk21-jammy AS builder
WORKDIR /workspace

# Copy Gradle root configuration and modules
COPY ferrox-java/settings.gradle.kts /workspace/settings.gradle.kts
COPY ferrox-java/build.gradle.kts /workspace/build.gradle.kts
COPY ferrox-java/gradle /workspace/gradle
COPY ferrox-java/gradlew /workspace/gradlew
COPY ferrox-java/gradlew.bat /workspace/gradlew.bat

# Copy subprojects
COPY ferrox-java/ferrox-java-core /workspace/ferrox-java-core
COPY ferrox-java/ferrox-java-security /workspace/ferrox-java-security
COPY ferrox-java/ferrox-java-cqrs /workspace/ferrox-java-cqrs
COPY ferrox-java/ferrox-java-data /workspace/ferrox-java-data
COPY ferrox-java/ferrox-java-offheap /workspace/ferrox-java-offheap
COPY ferrox-java/ferrox-java-resilience /workspace/ferrox-java-resilience
COPY ferrox-java/ferrox-java-observability /workspace/ferrox-java-observability
COPY ferrox-java/ferrox-java-utils /workspace/ferrox-java-utils
COPY ferrox-java/ferrox-java-web /workspace/ferrox-java-web
COPY ferrox-java/ferrox-java-dashboard /workspace/ferrox-java-dashboard
COPY ferrox-java/ferrox-starter /workspace/ferrox-starter
COPY ferrox-java/ferrox-java-showcase /workspace/ferrox-java-showcase

# Build production executable bootJar
RUN gradle :ferrox-java-showcase:bootJar -x test --no-daemon

# Stage 2: Minimal Eclipse Temurin 21 JRE Runtime
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Create non-root system user
RUN groupadd -r ferrox && useradd -r -g ferrox ferrox

# Copy compiled jar
COPY --from=builder /workspace/ferrox-java-showcase/build/libs/*.jar /app/ferrox-java-showcase.jar

# Configuration
ENV SERVER_PORT=8082
ENV SPRING_THREADS_VIRTUAL_ENABLED=true
ENV JAVA_TOOL_OPTIONS="--enable-preview -XX:+UseZGC -XX:+ZGenerational -XX:+UnlockExperimentalVMOptions"

EXPOSE 8082
USER ferrox:ferrox

HEALTHCHECK --interval=15s --timeout=5s --retries=3 \
  CMD wget -q --spider http://localhost:8082/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-jar", "/app/ferrox-java-showcase.jar"]
