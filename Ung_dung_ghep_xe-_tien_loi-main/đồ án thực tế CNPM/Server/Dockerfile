# ===== STAGE 1: BUILD =====
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B
COPY src src
RUN mvn clean package -DskipTests

# ===== STAGE 2: RUN =====
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
