#FROM openjdk:21-jdk-slim-bullseye
FROM openjdk:21-jdk-bullseye

#
#WORKDIR /app
#
#COPY target/personalfinancetrackerspring-0.0.1-SNAPSHOT.jar /app/app.jar
#
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]
# Stage 1: Build stage

#FROM openjdk:21-jdk-slim
#FROM maven:3.8.7-openjdk-21-slim AS builder
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application and skip tests if desired
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM openjdk:21-jdk-slim-bullseye
WORKDIR /app

# Copy the built jar from the builder stage; adjust the path/name as needed.
COPY --from=builder /app/target/personalfinancetrackerspring-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port your application uses
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

