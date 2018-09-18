package issue;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;

@Controller("/")
public class HelloController {

    private final MyKafkaClient kafkaClient;

    public HelloController(MyKafkaClient kafkaClient) {
        this.kafkaClient = kafkaClient;
    }

    @Get
    @Produces(MediaType.TEXT_PLAIN)
    public String index() {
        return "Hello World";
    }


    @Post
    @Produces(MediaType.TEXT_PLAIN)
    public String send() {
        kafkaClient.send(String.valueOf(System.currentTimeMillis()), "568", "Hello World!");
        return "\"Hello World!\" sent to Kafka topic 'notifications'";
    }
}
