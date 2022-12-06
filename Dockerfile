FROM openjdk:8-jdk-alpine

EXPOSE 8181

ADD target/api-service.jar api-service.jar

ENTRYPOINT ["java","-jar","api-service.jar"]
