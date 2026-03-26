FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="edu-platform"
WORKDIR /app
COPY target/order-service-1.0.0.jar app.jar
EXPOSE 8083
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
