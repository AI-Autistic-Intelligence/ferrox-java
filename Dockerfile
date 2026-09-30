# Stage 1: Build the multi-module project
FROM gradle:8.4-jdk21-alpine AS builder
WORKDIR /app
COPY . .
# We build the showcase app
RUN gradle :ferrox-java-showcase:bootJar --no-daemon || gradle build --no-daemon

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# We blindly copy the jar (we assume spring boot or shaded jar in build/libs)
COPY --from=builder /app/ferrox-java-showcase/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
