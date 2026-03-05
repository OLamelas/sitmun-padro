# Stage 0, "build padro", based on OpenJDK 17, to build and compile the application
ARG JDK_VERSION=17
ARG JDK_TAG=0.17
ARG ALPINE_TAG=3.22

# Use OpenJDK 17 as base image
FROM amazoncorretto:${JDK_VERSION}.${JDK_TAG} AS build-padro
COPY . /usr/src/padro
WORKDIR /usr/src/padro
# Build JAR (Docker only supports JAR packaging)
# For WAR builds, use: ./gradlew build -Ppackaging=war locally
RUN chmod +x gradlew
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon -i build -x test
ARG JDK_VERSION=17
ARG JDK_TAG=0.17
ARG ALPINE_TAG=3.22

# Stage 1, based on OpenJDK 17, to have only the compiled app

# Use OpenJDK 17 as base image
FROM amazoncorretto:${JDK_VERSION}.${JDK_TAG}-alpine${ALPINE_TAG}

# Create application directories
RUN mkdir -p /app/config

# Copy the built JAR file
COPY --from=build-padro /usr/src/padro/build/libs/*.jar /app/padro.jar

# Set working directory
WORKDIR /app

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Default JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Default Spring profile
ENV SPRING_PROFILES_ACTIVE="prod"

# Entry point with support for external configuration
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar padro.jar --spring.config.additional-location=file:/app/config/"]

