# Multi-stage build for optimized image size and security
# Stage 1: Build stage with Maven cache
FROM eclipse-temurin:21-jdk-alpine AS deps

# Install dependencies for building
RUN apk add --no-cache bash

WORKDIR /build

# Copy only pom.xml first to leverage Docker cache
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies - this layer will be cached unless pom.xml changes
RUN ./mvnw dependency:go-offline -B

# Stage 2: Build application
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

# Copy dependencies from previous stage
COPY --from=deps /root/.m2 /root/.m2

# Copy maven wrapper and pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy source code
COPY src src

# Build application - skip tests for faster build
RUN ./mvnw clean package -DskipTests -B && \
    mkdir -p target/dependency && \
    (cd target/dependency; jar -xf ../*.jar)

# Stage 3: Extract layers for better caching
FROM eclipse-temurin:21-jdk-alpine AS layers

WORKDIR /app

# Copy the jar from build stage
COPY --from=build /build/target/*.jar app.jar

# Extract layers using Spring Boot's layertools
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 4: Final runtime image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Add non-root user for security
RUN addgroup -g 1001 spring && \
    adduser -u 1001 -G spring -s /bin/sh -D spring

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

WORKDIR /app

# Copy layers in order of change frequency (least to most)
COPY --from=layers /app/dependencies/ ./
COPY --from=layers /app/spring-boot-loader/ ./
COPY --from=layers /app/snapshot-dependencies/ ./
COPY --from=layers /app/application/ ./

# Set permissions
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Run the application with optimized JVM flags
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:InitialRAMPercentage=50.0", \
     "-XX:+UseG1GC", \
     "-XX:+UseStringDeduplication", \
     "-XX:+OptimizeStringConcat", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.backgroundpreinitializer.ignore=true", \
     "org.springframework.boot.loader.launch.JarLauncher"]