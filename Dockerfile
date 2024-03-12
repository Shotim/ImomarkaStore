FROM openjdk:17-oracle
MAINTAINER shostkotimofei@gmail.com
COPY .mvn .mvn
COPY src src
COPY lombok.config lombok.config
COPY pom.xml pom.xml
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
EXPOSE 8080
ENTRYPOINT ["./mvnw","spring-boot:run"]