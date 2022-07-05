FROM openjdk:17-alpine

RUN apk add --no-cache gcompat libstdc++

WORKDIR /home/app/
COPY target/*.jar /home/app/app.jar
ENTRYPOINT java -jar app.jar
