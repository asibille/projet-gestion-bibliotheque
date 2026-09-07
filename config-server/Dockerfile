FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# config-repo est copie depuis le contexte de build racine (voir docker-compose.yml)
COPY config-repo ./config-repo
ENV SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=file:./config-repo
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
