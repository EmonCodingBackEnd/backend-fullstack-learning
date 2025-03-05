#FROM openjdk:8u212-jre-alpine
FROM arm64v8/openjdk:8-jdk
MAINTAINER 问秋 liming2011071@163.com
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
EXPOSE 8080

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar"]
