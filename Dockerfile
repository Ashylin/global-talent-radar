FROM node:24-alpine AS frontend
WORKDIR /ui
COPY frontend/package*.json ./
RUN npm ci --no-fund --no-audit
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk AS backend
WORKDIR /build
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src
COPY --from=frontend /ui/dist ./frontend/dist
RUN chmod +x gradlew && ./gradlew --no-daemon test bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system radar && useradd --system --gid radar radar
COPY --from=backend /build/build/libs/global-talent-radar.jar app.jar
USER radar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
