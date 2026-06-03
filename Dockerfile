# syntax=docker/dockerfile:1

# ---- Build stage: compile & package with Maven + JDK 21 ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Resolve dependencies first (cached layer unless pom.xml changes)
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Build the application (tests use in-memory H2; skipped here for image build speed)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Run stage: slim JRE with just the jar ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Non-root runtime user
RUN groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build /build/target/meetings-mom-governance-*.jar app.jar

# App listens on 8080 under context-path /meetings (see application.properties)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
