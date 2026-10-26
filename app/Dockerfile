
FROM node:20-alpine AS frontend-builder
WORKDIR /workspace/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk-alpine AS backend-builder
WORKDIR /workspace

COPY gradlew settings.gradle.kts* build.gradle.kts* ./
COPY gradle gradle
COPY app/build.gradle.kts app/
COPY app/src app/src

COPY --from=frontend-builder /workspace/frontend/dist app/src/main/resources/static/

RUN chmod +x ./gradlew
RUN ./gradlew :app:clean :app:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    mkdir -p /app/logs && chown -R appuser:appgroup /app

USER appuser

COPY --from=backend-builder /workspace/app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]