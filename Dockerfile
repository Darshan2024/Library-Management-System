# FROM maven:3.9-eclipse-temurin-17-alpine AS build
# ENV HOME=/build
# WORKDIR $HOME
# COPY pom.xml .
# RUN mvn dependency:go-offline -B
# COPY src ./src
# RUN --mount=type=cache,target=/root/.m2 mvn clean package

# FROM eclipse-temurin:17-jre-alpine
# RUN addgroup -S spring && adduser -S spring -G spring
# USER spring:spring
# WORKDIR /app
# ARG JAR_FILE=/build/target/*-SNAPSHOT.jar
# COPY --from=build --chown=spring:spring $JAR_FILE runner.jar
# RUN mkdir data
# ENV SPRING_DATASOURCE_URL=jdbc:sqlite:data/librarymanagementsystem.db
# EXPOSE 8082
# ENV JAVA_OPTS="-Xms256m -Xmx512m"
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/runner.jar"]

# -------- Build stage --------
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

# Cache deps
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Build (runs tests). Add -DskipTests if you need to.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B clean package

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-alpine

# Non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy fat jar
ARG JAR_FILE=/build/target/*-SNAPSHOT.jar
COPY --from=build --chown=spring:spring ${JAR_FILE} app.jar

# SQLite will live here
RUN mkdir -p /app/data
VOLUME ["/app/data"]

# Default DB (matches your application.properties name)
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/app/data/librarymanagementsystem.db
ENV JAVA_OPTS=""

# Local default 8082; Render will inject $PORT later
EXPOSE 8082
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8082} $JAVA_OPTS -jar /app/app.jar"]
