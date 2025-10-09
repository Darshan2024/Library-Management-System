FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B clean package

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

ARG JAR_FILE=/build/target/*-SNAPSHOT.jar
COPY --from=build --chown=spring:spring ${JAR_FILE} app.jar

RUN mkdir -p /app/data
VOLUME ["/app/data"]

ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/app/data/librarymanagementsystem.db
ENV JAVA_OPTS=""

EXPOSE 8082
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8082} $JAVA_OPTS -jar /app/app.jar"]
