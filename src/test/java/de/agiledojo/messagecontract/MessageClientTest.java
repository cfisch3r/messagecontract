package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageClientTest {

    private static final String QUEUE = "myQueue";

    private static final String EXCHANGE = "myExchange";

    @ClassRule
    public static DockerRule rabbitRule =
            DockerRule.builder()
                    .image("rabbitmq:latest")
                    .ports("5672")
                    .waitForPort("5672/tcp")
                    .waitForLog("Server startup complete")
                    .build();

    private static ConnectionFactory connectionFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        connectionFactory = connectionFactory();
        setUpRouting();
    }

    @Test
    public void canCreateQueue() {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(new Queue(QUEUE));
        Properties queueProperties = admin.getQueueProperties(QUEUE);
        assertThat(queueProperties).isNotNull();
    }

    @Test
    public void canSendMessage() {
        RabbitTemplate rabbitClient = new RabbitTemplate(connectionFactory);
        rabbitClient.convertAndSend(EXCHANGE,"foo.bar","my message");
        Object message = rabbitClient.receiveAndConvert(QUEUE);
        assertThat(message).isNotNull();
    }

    private static void setUpRouting() {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        Queue queue = new Queue(QUEUE);
        admin.declareQueue(queue);
        TopicExchange exchange = new TopicExchange(EXCHANGE);
        admin.declareExchange(exchange);
        admin.declareBinding(
                BindingBuilder.bind(queue).to(exchange).with("foo.*"));
    }

    //    @Test
//    public void canSendAndReceiveMessage() {
//        RabbitAdmin admin = new RabbitAdmin(connectionFactory());
//        admin.declareQueue(new Queue("myQueue"));
//        RabbitTemplate rabbit = new RabbitTemplate(connectionFactory());
//
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.addQueueNames("myQueue");
//        container.setConnectionFactory(connectionFactory());
//        final StringBuilder body = new StringBuilder();
//        container.setMessageListener(new MessageListener() {
//            public void onMessage(Message message) {
//                body.append(message.getBody());
//            }
//        });
//        Assertions.assertThat(body).isNotEmpty();
//    }

    private static ConnectionFactory connectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setHost(rabbitRule.getDockerHost());
        cf.setPort(rabbitRule.getHostPort("5672/tcp"));
        return cf;
    }
}
