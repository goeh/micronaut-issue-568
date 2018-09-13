# Test project to reproduce Micronaut issue [568](https://github.com/micronaut-projects/micronaut-core/issues/568)

1. cd eureka && ./gradlew docker
2. cd micronaut && ./gradlew docker
3. docker-compose up

Eureka starts but Micronaut service shuts down because it cannot connect to Eureka.

Wait 30 seconds then do:

`docker-compose up issue568-micronaut`

Micronaut will now register with Eureka.


## RTFM

Micronaut already support configurable retries!
It's documented in the [Consul section](https://docs.micronaut.io/latest/guide/index.html#_customizing_consul_service_registration) of the user guide.

With the following configuration in the Micronaut service it will successfully register with Eureka after 20-30 seconds.

```
eureka:
    client:
        registration:
            enabled: true
            fail-fast: false
            retry-count: 10
            retry-delay: 5s
        defaultZone: "${EUREKA_HOST:localhost}:${EUREKA_PORT:8761}"
```

