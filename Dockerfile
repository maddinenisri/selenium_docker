# =========================
# STAGE 1 — Build the Java Project
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Build project and copy dependencies
RUN mvn clean compile dependency:copy-dependencies

# =========================
# STAGE 2 — Create a Slim Runner Image
# =========================
FROM selenium/standalone-chromium:latest

USER root

RUN apt-get update && apt-get install -y \
    ca-certificates \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

USER seluser

WORKDIR /home/seluser/selenium_project

# Copy built application and dependencies
COPY --from=builder /build/target/classes ./classes
COPY --from=builder /build/target/dependency ./dependency

ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROMEDRIVER_BIN=/usr/bin/chromedriver

# Set classpath: classes + all dependency jars
CMD ["java", "-cp", "classes:dependency/*", "com.mdstech.GoogleTest"]
