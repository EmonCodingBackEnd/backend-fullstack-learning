FROM java:8
MAINTAINER 问秋 liming2011071@163.com
EXPOSE 8080

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar", "--spring.profiles.active=prod"]
