FROM amazoncorretto:11-alpine3.13 as corretto-jdk
RUN apk add --no-cache binutils
RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

FROM alpine:latest
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=corretto-jdk /customjre $JAVA_HOME

WORKDIR /app
EXPOSE 8080
COPY target/brokerage-*.jar ./app.jar
ENTRYPOINT ["java","-jar","app.jar"]
