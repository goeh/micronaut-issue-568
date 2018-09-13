# Test project to reproduce Micronaut issue [568](https://github.com/micronaut-projects/micronaut-core/issues/568)

1. cd eureka && ./gradlew docker
2. cd micronaut && ./gradlew docker
3. cd gateway && ./gradlew docker
4. docker-compose up

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

# NEXT problem!

Spring Cloud Gateway uses UPPERCASE hostname when connecting to hosts discovered by Eureka

Eureka has always reported hostnames in uppercase. I guess Spring Cloud Gateway needs to handle that.

$ http :9000/
```
HTTP/1.1 500 Internal Server Error
Content-Length: 131
Content-Type: application/json;charset=UTF-8

{
    "error": "Internal Server Error", 
    "message": "ISSUE568-MICRONAUT", 
    "path": "/", 
    "status": 500, 
    "timestamp": "2018-09-13T20:21:00.537+0000"
}
```

If you look in the gateway log you will see:
```
issue568-gateway_1    | java.net.UnknownHostException: ISSUE568-MICRONAUT
issue568-gateway_1    | 	at java.net.InetAddress.getAllByName0(InetAddress.java:1280) ~[na:1.8.0_181]
issue568-gateway_1    | 	at java.net.InetAddress.getAllByName(InetAddress.java:1192) ~[na:1.8.0_181]
issue568-gateway_1    | 	at java.net.InetAddress.getAllByName(InetAddress.java:1126) ~[na:1.8.0_181]
issue568-gateway_1    | 	at java.net.InetAddress.getByName(InetAddress.java:1076) ~[na:1.8.0_181]
issue568-gateway_1    | 	at io.netty.util.internal.SocketUtils$8.run(SocketUtils.java:146) ~[netty-common-4.1.29.Final.jar!/:4.1.29.Final]
issue568-gateway_1    | 	at io.netty.util.internal.SocketUtils$8.run(SocketUtils.java:143) ~[netty-common-4.1.29.Final.jar!/:4.1.29.Final]
issue568-gateway_1    | 	at java.security.AccessController.doPrivileged(Native Method) ~[na:1.8.0_181]
issue568-gateway_1    | 	at io.netty.util.internal.SocketUtils.addressByName(SocketUtils.java:143) ~[netty-common-4.1.29.Final.jar!/:4.1.29.Final]
```
