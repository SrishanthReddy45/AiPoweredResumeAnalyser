# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cached layer)
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S resumeiq && adduser -S resumeiq -G resumeiq

# Copy the fat JAR from the build stage
COPY --from=build /app/target/resumeiq-0.0.1-SNAPSHOT.jar app.jar

# Create storage directory
RUN mkdir -p /app/storage && chown -R resumeiq:resumeiq /app

USER resumeiq

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
