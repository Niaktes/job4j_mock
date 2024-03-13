# Stage 1 - packaging jar
FROM maven:3.6.3-openjdk-17 AS maven
RUN mkdir /auth
WORKDIR /auth
COPY . /auth
RUN mvn package -Dmaven.test.skip=true

# Stage 2 - updating liquibase
FROM openjdk:17.0.2-jdk
WORKDIR /auth
CMD mvn liquibase:update -Pdocker

# Stage 3 - running project
FROM openjdk:17.0.2-jdk
WORKDIR /auth
COPY --from=maven /auth/target/auth-1.0.0.jar auth.jar
CMD java -jar auth.jar