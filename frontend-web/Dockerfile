FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY frontend-web/pom.xml ./frontend-web/
COPY frontend-web/src ./frontend-web/src/

RUN mvn -f frontend-web/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/frontend-web/target/*.jar app.jar

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]