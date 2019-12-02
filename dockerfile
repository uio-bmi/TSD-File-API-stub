FROM maven:3.6.0-jdk-13-alpine AS MAVEN_BUILD
COPY . .
RUN mvn package
FROM openjdk:13-alpine
COPY --from=MAVEN_BUILD /target/TSD-File-API-stub-0.0.1-SNAPSHOT.jar /TSD-File-API-stub.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "TSD-File-API-stub.jar"]