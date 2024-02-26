FROM openjdk:17-jdk-alpine
MAINTAINER shostkotimofei@gmail.com
WORKDIR /app
COPY target/InomarkaStore-0.0.1-SNAPSHOT.jar /app
COPY .env /app/.env
EXPOSE 8080
ENTRYPOINT ["java","-jar","/InomarkaStore-0.0.1-SNAPSHOT.jar"]