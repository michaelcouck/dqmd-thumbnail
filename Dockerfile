FROM openjdk:11-jre-slim

RUN apt-get update && apt-get install -y imagemagick

COPY target/thumbnail-1.0.0-SNAPSHOT.jar /thumbnail-1.0.0-SNAPSHOT.jar
CMD java -Dspring.profiles.active=dev -jar /thumbnail-1.0.0-SNAPSHOT.jar