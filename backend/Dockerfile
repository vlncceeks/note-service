FROM maven:3.9.9-eclipse-temurin-22 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:22.0.2_9-jre-alpine
WORKDIR /app
COPY --from=build /src/target/app.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:MaxRAMPercentage=70"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
