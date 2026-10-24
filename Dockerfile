# ==============================================================================
# Multi-Stage Dockerfile for Campus Giveaway
# Single deployable container on Google Cloud Run
# Stage 1: Node 22 - Angular 20 production build
# Stage 2: Maven / JDK 21 - Spring Boot 3.5 fat JAR packaging with static assets
# Stage 3: Distroless Java 21 - Minimal, hardened non-root container runtime
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build Angular Frontend
# ------------------------------------------------------------------------------
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build -- --configuration=production

# ------------------------------------------------------------------------------
# Stage 2: Build Spring Boot Backend with Bundled Static Assets
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app/backend

# Copy Maven wrapper and dependencies specification
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x ./mvnw

# Pre-fetch dependencies for layer caching
RUN ./mvnw dependency:go-offline -B || true

# Copy backend source code
COPY backend/src/ src/

# Bundle frontend production build into Spring Boot static resources directory
COPY --from=frontend-build /app/frontend/dist/frontend/browser/ src/main/resources/static/

# Package executable fat JAR (skipping test execution inside container build)
RUN ./mvnw clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 3: Hardened Distroless Runtime
# ------------------------------------------------------------------------------
FROM gcr.io/distroless/java21-debian12:nonroot AS runtime
WORKDIR /app

# Copy packaged JAR from backend build stage
COPY --from=backend-build /app/backend/target/*.jar /app/app.jar

# Cloud Run binds to port 8080 by default
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

USER nonroot:nonroot

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
