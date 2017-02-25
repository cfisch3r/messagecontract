package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

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
        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setHost(rabbitRule.getDockerHost());
        cf.setPort(rabbitRule.getHostPort("5672/tcp"));


        // set up the queue, exchange, binding on the broker
        RabbitAdmin admin = new RabbitAdmin(cf);
        Queue queue = new Queue("myQueue");
        admin.declareQueue(queue);
        Properties queueProperties = admin.getQueueProperties("myQueue");
        Assertions.assertThat(queueProperties).isNotNull();
    }
}
