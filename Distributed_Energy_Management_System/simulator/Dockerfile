# Build stage (Consistent with M1, M2, M3)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage (Consistent with M1, M2, M3)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/simulator-1.0-SNAPSHOT.jar app.jar
# Environment variable to find RabbitMQ in Docker
ENV RABBIT_HOST=rabbitmq
CMD ["java", "-jar", "app.jar"]