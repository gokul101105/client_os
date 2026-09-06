# Module 18. Built from the REPO ROOT (see docker-compose.yml's
# `context: ..`), not just backend/ -- this image needs database/migrations
# too, which lives outside the backend/ directory on disk. Flyway's
# filesystem-based migration path (see application.properties) only works
# locally because backend/ and database/ are sibling folders on one
# machine; inside the container there's no such sibling relationship, so
# migrations are copied in and SPRING_FLYWAY_LOCATIONS points at their
# in-image location instead.

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml backend/mvnw ./
COPY backend/.mvn .mvn
RUN ./mvnw -q -B dependency:go-offline
COPY backend/src src
RUN ./mvnw -q -B package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY database/migrations /database/migrations
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
