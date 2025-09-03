####
# This Dockerfile is used to build a runnable image of the application.
# Build stage
####
FROM gradle:8.10-jdk21 AS build

# Set the working directory
WORKDIR /app

# Copy gradle files
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle gradle.properties ./
COPY gradle/libs.versions.toml gradle/libs.versions.toml

# Copy lombok configuration
COPY lombok.config ./

# Download dependencies (this layer is cached unless dependencies change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew build -x test --no-daemon

####
# Runtime stage
####
FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.18

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=build --chown=185 /app/build/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/build/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/build/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8081
USER 185

# Set the PORT environment variable to match Railway's expectations
ENV PORT=8081

# Update the application.properties port to use the PORT environment variable
ENV QUARKUS_HTTP_PORT=${PORT:-8081}

# Set production profile for Railway deployment
ENV QUARKUS_PROFILE=prod

# Explicitly declare environment variables that Railway should provide
# This ensures they're available in the container at runtime
ENV DATABASE_URL=""
ENV PGHOST=""
ENV PGPORT=""
ENV PGDATABASE=""
ENV PGUSER=""
ENV PGPASSWORD=""
ENV TWELVE_DATA_API_KEY=""

ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
