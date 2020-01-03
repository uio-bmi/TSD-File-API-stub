FROM maven:3.6.0-jdk-13-alpine as builder
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ /src/
RUN mvn clean install -Dmaven.javadoc.skip=true -B -V -DskipDockerPush -DskipDockerBuild

FROM openjdk:13-alpine

COPY --from=builder /target/*-SNAPSHOT.jar /stub.jar

CMD ["java", "-jar", "/stub.jar"]
