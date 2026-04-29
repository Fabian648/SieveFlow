# 1. Stage: Build mit Gradle
FROM gradle:8-jdk24 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew build --no-daemon

# 2. Stage: Leichtgewichtige Runtime
FROM eclipse-temurin:24-jre-alpine
EXPOSE 1337
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/prime-server.jar
ENTRYPOINT ["java", "-jar", "/app/prime-server.jar"]