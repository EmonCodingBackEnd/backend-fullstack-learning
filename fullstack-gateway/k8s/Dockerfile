FROM openjdk:8u212-jre-alpine
MAINTAINER 问秋 liming2011071@163.com
EXPOSE 8080

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar"]
