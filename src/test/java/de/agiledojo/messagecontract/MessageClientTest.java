package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Properties;

public class MessageClientTest {

    @ClassRule
    public static DockerRule rabbitRule =
            DockerRule.builder()
                    .image("rabbitmq:latest")
                    .ports("5672")
                    .waitForPort("5672/tcp")
                    .waitForLog("Server startup complete")
                    .build();

    @Test
    public void canCreateQueue() {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory());
        admin.declareQueue(new Queue("myQueue"));
        Properties queueProperties = admin.getQueueProperties("myQueue");
        Assertions.assertThat(queueProperties).isNotNull();
    }

    @Test
    public void canSendMessage() {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory());
        Queue queue = new Queue("myQueue");
        admin.declareQueue(queue);
        TopicExchange exchange = new TopicExchange("myExchange");
        admin.declareExchange(exchange);
        admin.declareBinding(
                BindingBuilder.bind(queue).to(exchange).with("foo.*"));

        RabbitTemplate rabbitClient = new RabbitTemplate(connectionFactory());
        rabbitClient.convertAndSend("myExchange","foo.bar","my message");
        rabbitClient.setQueue("myQueue");
        Object message = rabbitClient.receiveAndConvert();
        Assertions.assertThat(message).isNotNull();
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

    private CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setHost(rabbitRule.getDockerHost());
        cf.setPort(rabbitRule.getHostPort("5672/tcp"));
        return cf;
    }
}
