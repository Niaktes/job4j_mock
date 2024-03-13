# Stage 1 - packaging jar
FROM maven:3.6.3-openjdk-17 AS maven
RUN mkdir /site
WORKDIR /site
COPY . /site
RUN mvn package -Dmaven.test.skip=true

# Stage 2 - running project
FROM openjdk:17.0.2-jdk
WORKDIR /site
COPY --from=maven /site/target/site-1.0.0.jar site.jar
CMD java -jar site.jar