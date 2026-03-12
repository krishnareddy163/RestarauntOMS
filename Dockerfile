# Multi-stage build for RestaurantOS
FROM gradle:9.3.0-jdk17 AS builder

WORKDIR /home/gradle/project

# Copy gradle files
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle gradlew .
COPY --chown=gradle:gradle build.gradle .
COPY --chown=gradle:gradle settings.gradle .

# Copy source code
COPY --chown=gradle:gradle src src

# Build the application
RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy JAR from builder
COPY --from=builder /home/gradle/project/build/libs/demo-security-1.0.0.jar app.jar

# Create non-root user
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

# Set ownership
RUN chown -R appuser:appuser /app

USER appuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/api/v1/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
