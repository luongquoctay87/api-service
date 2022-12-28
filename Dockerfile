FROM openjdk:11

ARG JAR_FILE=target/api-service.jar

ADD ${JAR_FILE} api-service.jar

EXPOSE 8181

ENTRYPOINT ["java","-jar","api-service.jar"]
