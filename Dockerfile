# ============================================================================
# Sistema de Gestao Academica - imagem unica (frontend Angular + backend)
# ============================================================================

# 1) Build do frontend Angular
FROM node:22-alpine AS frontend
WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm ci --no-audit --no-fund

COPY frontend/ ./
RUN npm run build

# 2) Build do backend Spring Boot com o SPA em src/main/resources/static
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY backend/src ./src
COPY --from=frontend /frontend/dist/frontend/browser ./src/main/resources/static
RUN mvn -B -q package -DskipTests

# 3) Runtime
FROM eclipse-temurin:21-jre

RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar
RUN chown app:app app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
