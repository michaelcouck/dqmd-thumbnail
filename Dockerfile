FROM openjdk:8-jdk-alpine
COPY target/thumbnail-1.0.0-SNAPSHOT.jar /thumbnail-1.0.0-SNAPSHOT.jar
CMD java -jar /thumbnail-1.0.0-SNAPSHOT.jar