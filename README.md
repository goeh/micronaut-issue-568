# Test project to reproduce Micronaut issue [568](https://github.com/micronaut-projects/micronaut-core/issues/568)

1. cd eureka && ./gradlew docker
2. cd micronaut && ./gradlew docker
3. docker-compose up

Eureka starts but Micronaut service shuts down because it cannot connect to Eureka.

Wait 30 seconds then do:

`docker-compose up issue568-micronaut`

Micronaut will now register with Eureka.


