FROM amazoncorretto:11-alpine3.13
WORKDIR /app
EXPOSE 8080
COPY target/brokerage-*.jar ./app.jar
ENTRYPOINT ["java","-jar","app.jar"]
