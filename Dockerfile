FROM eclipse-temurin:17-jre-focal

COPY target/SpringWeb-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]