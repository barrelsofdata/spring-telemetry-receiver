# Spring Kafka based Telemetry Data Receiver
The project is built on spring boot using spring kafka, to receive telemetry data on a REST endpoint and publish the same to a Kafka topic. The related blog post can be found at [https://www.barrelsofdata.com/spring-boot-based-telemetry-data-receiver-api-spring-kafka-producer/](https://www.barrelsofdata.com/spring-boot-based-telemetry-data-receiver-api-spring-kafka-producer/)

## Build instructions
From the root of the project execute the below commands
- To clear all compiled classes, build and log directories
```shell script
./gradlew clean
```
- To run tests
```shell script
./gradlew test
```
- To build jar
```shell script
./gradlew bootJar
```
- Build OCI compliant image
```shell script
./gradlew bootBuildImage
```
- All combined
```shell script
./gradlew clean test bootBuildImage
```
- Run from IDE
```shell script
./gradlew bootRun -PjvmArgs="-D--spring.config.location=config/dev.properties"
```
- Run SonarQube Code Analysis
```
docker run -d --rm --name sonarqube -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9000:9000 sonarqube:community
./gradlew sonarqube
```
## Run native
```shell script
java -jar build/libs/spring-telemetry-receiver-1.0.jar --spring.config.location=config/dev.properties
```

## Run as docker container
```shell script
docker run -itd --rm --network host --mount type=bind,source=$(pwd)/config/dev.properties,target=/application.properties,readonly -e JAVA_OPTS=-D--spring.config.location=/application.properties --name spring-telemetry-server spring-telemetry-receiver:1.0
```