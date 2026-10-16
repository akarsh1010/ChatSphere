FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN javac -d out/production/CoreChat src/corechat/*.java
EXPOSE 8080
CMD ["java", "-cp", "out/production/CoreChat", "corechat.ChatWebServer"]
