# Stage 1 - packaging jar
FROM maven:3.6.3-openjdk-17 AS maven
RUN mkdir /desc
WORKDIR /desc
COPY . /desc
RUN mvn package -Dmaven.test.skip=true

# Stage 2 - updating liquibase
FROM openjdk:17.0.2-jdk
WORKDIR /desc
CMD mvn liquibase:update -Pdocker

# Stage 3 - running project
FROM openjdk:17.0.2-jdk
WORKDIR /desc
COPY --from=maven /desc/target/desc-1.0.0.jar desc.jar
CMD java -jar desc.jar