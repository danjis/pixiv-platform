FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="pixiv-platform"
LABEL service="commission-service"

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY target/commission-service-*.jar app.jar

EXPOSE 8083

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=dev
ENV NACOS_ADDR=47.120.61.152:8848

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dspring.cloud.nacos.discovery.server-addr=$NACOS_ADDR -Dspring.cloud.nacos.config.server-addr=$NACOS_ADDR -Dspring.cloud.nacos.discovery.ip=$HOST_IP -jar app.jar"]
