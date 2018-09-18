# Test project to reproduce Micronaut issue [568](https://github.com/micronaut-projects/micronaut-core/issues/568)

1. ./gradlew docker
2. docker-compose up

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

# ResourceLeakDetector issue

```
  | 07:35:34.162 [nioEventLoopGroup-1-7] ERROR io.netty.util.ResourceLeakDetector - LEAK: ByteBuf.release() was not called before it's garbage-collected. See http://netty.io/wiki/reference-counted-objects.html for more information.
  | Recent access records: 
  | Created at:
  | 	io.netty.buffer.AbstractByteBufAllocator.compositeDirectBuffer(AbstractByteBufAllocator.java:221)
  | 	io.netty.buffer.AbstractByteBufAllocator.compositeBuffer(AbstractByteBufAllocator.java:199)
  | 	io.netty.handler.codec.MessageAggregator.decode(MessageAggregator.java:255)
  | 	io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:88)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
  | 	io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:340)
  | 	io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:102)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
  | 	io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:340)
  | 	io.netty.channel.CombinedChannelDuplexHandler$DelegatingChannelHandlerContext.fireChannelRead(CombinedChannelDuplexHandler.java:438)
  | 	io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:310)
  | 	io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:284)
  | 	io.netty.channel.CombinedChannelDuplexHandler.channelRead(CombinedChannelDuplexHandler.java:253)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
  | 	io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:340)
  | 	io.netty.handler.timeout.IdleStateHandler.channelRead(IdleStateHandler.java:286)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
  | 	io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:340)
  | 	io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1434)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
  | 	io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
  | 	io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:965)
  | 	io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:163)
  | 	io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:646)
  | 	io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:581)
  | 	io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:498)
  | 	io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:460)
  | 	io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:884)
  | 	io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
  | 	java.lang.Thread.run(Thread.java:748)

```

```
./gradlew docker
docker-compose up
curl -X POST http://localhost:9000/
```
