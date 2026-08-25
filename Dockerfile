# syntax=docker/dockerfile:1

# ============================================================
# Build stage
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy dependency descriptor first so Docker can cache dependency
# resolution when application source files change.
COPY pom.xml .

RUN mvn -B dependency:go-offline

# Copy application source only after dependencies are cached.
COPY src ./src

RUN mvn -B clean package -DskipTests


# ============================================================
# Runtime stage
# ============================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Run the application as a non-root user.
RUN groupadd --system spring     && useradd --system --gid spring spring     && mkdir -p /app/logs     && chown -R spring:spring /app

COPY --from=build /build/target/*.jar /app/payflow.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/payflow.jar"]
