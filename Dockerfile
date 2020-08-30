FROM openjdk:8
ADD target/watson-assistant-api-0.0.1-SNAPSHOT.jar watson-assistant-api-0.0.1-SNAPSHOT.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "watson-assistant-api-0.0.1-SNAPSHOT.jar"]