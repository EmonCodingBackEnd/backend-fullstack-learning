FROM openjdk:8u212-jre-alpine
MAINTAINER 问秋 liming2011071@163.com
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 安装字体工具包
RUN apk add --no-cache fontconfig
# 添加宋体字体文件到容器中
COPY dockerfiles/simsun.ttf /usr/share/fonts/SimSun.ttf
# 配置字体环境变量
RUN mkfontscale && mkfontdir && fc-cache -fv
# 设置字体环境变量
ENV FONTCONFIG_FILE=/etc/fonts/fonts.conf
EXPOSE 8080

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar"]
