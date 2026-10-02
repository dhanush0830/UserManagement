# ============================================================
# Multi-stage Dockerfile for User Management System
# Stage 1 — Build: compile and package an executable uber JAR
# Stage 2 — Run:  minimal JRE image for Railway deployment
# ============================================================

# ── Stage 1: Build ──────────────────────────────────────────
FROM eclipse-temurin:22-jdk-alpine AS builder

WORKDIR /build

# Copy dependency manifests first to leverage Docker layer caching.
# Dependencies are only re-downloaded when pom.xml changes.
COPY pom.xml .
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B --quiet

# Copy the full source tree and build the uber JAR
COPY src ./src
RUN mvn package -DskipTests -B --quiet

# ── Stage 2: Runtime ────────────────────────────────────────
FROM eclipse-temurin:22-jre-alpine

LABEL maintainer="UserManagement"
LABEL description="Enterprise User Management System — embedded Tomcat on Railway"

WORKDIR /app

# Copy the shaded (uber) JAR from the build stage
COPY --from=builder /build/target/user-management.jar app.jar

# Copy the webapp directory so embedded Tomcat can serve JSPs and static files.
# The Main class looks for 'webapp/' relative to the working directory first.
COPY src/main/webapp ./webapp

# Railway dynamically assigns a PORT env var; the app already reads it.
# We expose 8080 as the documentation/default port.
EXPOSE 8080

# Use exec form (no shell) so signals (SIGTERM) reach the JVM cleanly.
# JVM flags:
#   -XX:+UseContainerSupport        — respect container CPU/memory limits
#   -XX:MaxRAMPercentage=75.0       — use up to 75 % of container RAM for heap
#   -Djava.net.preferIPv4Stack=true — avoids IPv6 issues in some PaaS environments
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.net.preferIPv4Stack=true", \
  "-jar", "app.jar"]
