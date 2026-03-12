FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="pixiv-platform"
LABEL service="notification-service"

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY target/notification-service-*.jar app.jar

EXPOSE 8084

ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=dev
ENV NACOS_ADDR=pixiv-nacos:8848

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dspring.cloud.nacos.discovery.server-addr=$NACOS_ADDR -Dspring.cloud.nacos.config.server-addr=$NACOS_ADDR -Dspring.cloud.nacos.discovery.ip=$HOST_IP -jar app.jar"]
