FROM maven:3.6.3-jdk-13 AS MAVEN_BUILD
COPY pom.xml /build/
COPY src /build/src/
WORKDIR /build/
RUN mvn package
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=MAVEN_BUILD /build/target/TSD-File-API-stub-0.0.1-SNAPSHOT.jar /app/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "TSD-File-API-stub-0.0.1-SNAPSHOT.jar"]