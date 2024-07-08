# FROM 192.168.200.116:5080/devops-learning/openjdk:8-jre
#FROM openjdk:8-jre
FROM gaia-e2-01-registry.cn-shanghai.cr.aliyuncs.com/lishi/openjdk:8-jre-custom
MAINTAINER 问秋 liming2011071@163.com

COPY huiba-gaia-admin-provider.jar huiba-gaia-admin-provider.jar

ENTRYPOINT ["java", "-jar", "/huiba-gaia-admin-provider.jar"]
