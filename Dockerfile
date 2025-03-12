FROM openjdk:21-jdk-slim-bullseye

WORKDIR /app

COPY target/personalfinancetrackerspring-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
