FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY build/libs/releaseguard-0.0.1.jar releaseguard-0.0.1.jar
EXPOSE 8080
CMD ["java", "-jar", "releaseguard-0.0.1.jar"]
