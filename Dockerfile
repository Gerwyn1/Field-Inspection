# Multi-stage build for the Hugging Face Space demo: one container that serves
# BOTH the Flutter web app and the Spring Boot API from the same origin.
#
# Stage 1 ─ compile the Flutter app to a static web bundle (HTML/JS/wasm)
FROM ghcr.io/cirruslabs/flutter:stable AS flutter-build
WORKDIR /build
COPY mobile/ .
RUN flutter pub get && flutter build web --release

# Stage 2 ─ build the Spring Boot jar, with the web bundle baked into /static
# (anything in src/main/resources/static is served as-is — Spring's version of
# `app.use(express.static('build'))`)
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /build
COPY backend/pom.xml .
# Download dependencies as their own layer: unchanged pom.xml = cached deps,
# so code-only rebuilds don't re-download the internet.
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=flutter-build /build/build/web ./src/main/resources/static
RUN mvn -q -B -DskipTests package

# Stage 3 ─ slim runtime: just a JRE and the fat jar (no Maven, no Flutter SDK)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /build/target/*.jar app.jar
# The demo profile: in-memory H2, port 7860 (Hugging Face's expected app_port),
# JWT secret from the JWT_SECRET env var (a Space "secret").
ENV SPRING_PROFILES_ACTIVE=demo
EXPOSE 7860
# Hugging Face runs containers as a non-root user — don't assume root.
USER 1000
ENTRYPOINT ["java", "-jar", "app.jar"]
