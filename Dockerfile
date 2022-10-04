FROM openjdk:8-jdk-alpine

ARG JAR_FILE=target/api-service.jar

RUN mkdir /opt/api-service

COPY ${JAR_FILE} /opt/api-service/api-service.jar

ENTRYPOINT ["java","-jar","/opt/api-service/api-service.jar"]
