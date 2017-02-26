package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;

import com.rabbitmq.client.*;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Compatibility;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void canCreateQueue() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitRule.getDockerHost());
        factory.setPort(rabbitRule.getHostPort("5672/tcp"));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        String queue = channel.queueDeclare().getQueue();
        Assertions.assertThat(queue).isNotEmpty();
        channel.close();
        connection.close();
    }

    @Test
    public void canSendMessage() throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitRule.getDockerHost());
        factory.setPort(rabbitRule.getHostPort("5672/tcp"));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();





        String queue = channel.queueDeclare().getQueue();

        String message = "Hello, world!";
        channel.basicPublish("", queue, null, message.getBytes());
        channel.basicPublish("", queue, null, message.getBytes());

        System.out.println(channel.messageCount(queue));
        GetResponse response = channel.basicGet(queue, true);
        System.out.println(response);
        Assertions.assertThat(response).isNotNull();
        channel.close();
        connection.close();


    }

    //    @Test
//    public void canSendMessage() {
//        RabbitTemplate rabbitClient = new RabbitTemplate(connectionFactory);
//        rabbitClient.convertAndSend(EXCHANGE,"foo.bar","my message");
//        Object message = rabbitClient.receiveAndConvert(QUEUE);
//        assertThat(message).isNotNull();
//    }

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

//    private static void setUpRouting() {
//        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
//        Queue queue = new Queue(QUEUE);
//        admin.declareQueue(queue);
//        TopicExchange exchange = new TopicExchange(EXCHANGE);
//        admin.declareExchange(exchange);
//        admin.declareBinding(
//                BindingBuilder.bind(queue).to(exchange).with("foo.*"));
//    }
//
//
//    private static ConnectionFactory connectionFactory() {
//        CachingConnectionFactory cf = new CachingConnectionFactory();
//        cf.setHost(rabbitRule.getDockerHost());
//        cf.setPort(rabbitRule.getHostPort("5672/tcp"));
//        return cf;
//    }
}
