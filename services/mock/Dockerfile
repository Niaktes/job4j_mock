# Stage 1 - packaging jar
FROM maven:3.6.3-openjdk-17 AS maven
RUN mkdir /mock
WORKDIR /mock
COPY . /mock
RUN mvn package -Dmaven.test.skip=true

# Stage 2 - updating liquibase
FROM openjdk:17.0.2-jdk
WORKDIR /mock
CMD mvn liquibase:update -Pdocker

# Stage 3 - running project
FROM openjdk:17.0.2-jdk
WORKDIR /mock
COPY --from=maven /mock/target/mock-1.0.0.jar mock.jar
CMD java -jar mock.jar