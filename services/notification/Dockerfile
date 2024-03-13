# Stage 1 - packaging jar
FROM maven:3.6.3-openjdk-17 AS maven
RUN mkdir /notification
WORKDIR /notification
COPY . /notification
RUN mvn package -Dmaven.test.skip=true

# Stage 2 - updating liquibase
FROM openjdk:17.0.2-jdk
WORKDIR /notification
CMD mvn liquibase:update -Pdocker

# Stage 3 - running project
FROM openjdk:17.0.2-jdk
WORKDIR /notification
COPY --from=maven /notification/target/notification-1.0.0.jar notification.jar
CMD java -jar notification.jar