FROM openjdk:8u212-jre-alpine
MAINTAINER 问秋 liming2011071@163.com
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 安装字体工具包
# 切成国内阿里云源
RUN sed -i "s/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g" /etc/apk/repositories
# 安装中文字体 && 删除安装缓存包 && 刷新字体
RUN apk add --update ttf-dejavu fontconfig && rm -rf /var/cache/apk/* && mkfontscale && mkfontdir && fc-cache
# 添加宋体字体文件到容器中
#COPY dockerfiles/simsun.ttf /usr/share/fonts/SimSun.ttf
EXPOSE 8080

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar"]
