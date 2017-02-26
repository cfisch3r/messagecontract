package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;
import com.rabbitmq.client.*;
import org.junit.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class QueueDoubleTest {

    @ClassRule
    public static DockerRule rabbitRule =
            DockerRule.builder()
                    .image("rabbitmq:latest")
                    .ports("5672")
                    .waitForPort("5672/tcp")
                    .waitForLog("Server startup complete")
                    .build();

    private static ConnectionFactory connectionFactory;

    private Channel channel;

    private Connection connection;
    private String queue;

    @BeforeClass
    public static void setUpConnectionFactory() throws Exception {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitRule.getDockerHost());
        connectionFactory.setPort(rabbitRule.getHostPort("5672/tcp"));
    }

    @Before
    public void setUp() throws Exception {
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        queue = channel.queueDeclare().getQueue();
    }

    @After
    public void tearDown() throws Exception {
        channel.close();
        connection.close();
    }

    @Test
    public void queueIsCreated() throws IOException, TimeoutException {
        assertThat(queue).isNotEmpty();
    }

    @Test
    public void messageCanBeReceived() throws IOException, TimeoutException, InterruptedException {
        String message = "Hello, world!";
        channel.basicPublish("", queue, null, message.getBytes());
        GetResponse response = channel.basicGet(queue, true);
        assertThat(response).isNotNull();
    }

    @Test
    public void messageHasUserId() throws IOException {
        String message = "Hello, world!";
        channel.basicPublish("", queue, new AMQP.BasicProperties.Builder()
                .appId("app ID")
                .build(), message.getBytes());
        GetResponse response = channel.basicGet(queue, true);
        assertThat(response.getProps().getAppId()).isEqualTo("app ID");

    }
}
