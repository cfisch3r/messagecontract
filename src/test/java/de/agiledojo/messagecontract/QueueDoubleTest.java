package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;
import com.rabbitmq.client.*;
import org.junit.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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

    private static final Message sampleMessage = new Message("guest","App ID","json",
            StandardCharsets.UTF_8.name(),new Date(), "fsf");

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
        sendSampleMessage();
        GetResponse response = getMessageFromQueue();
        assertThat(response).isNotNull();
    }

    @Test
    public void messageHasAppId() throws IOException {
        sendSampleMessage();
        GetResponse response = getMessageFromQueue();
        assertThat(response.getProps().getAppId()).isEqualTo(sampleMessage.getAppId());

    }

    @Test
    public void messageHasUserId() throws IOException {
        sendSampleMessage();
        GetResponse response = getMessageFromQueue();
        assertThat(response.getProps().getUserId()).isEqualTo(sampleMessage.getUserId());

    }

    @Test
    public void messageHasContentType() throws IOException {
        sendSampleMessage();
        GetResponse response = getMessageFromQueue();
        assertThat(response.getProps().getContentType()).isEqualTo(sampleMessage.getContentType());

    }
    private GetResponse getMessageFromQueue() throws IOException {
        return channel.basicGet(queue, true);
    }

    private void sendSampleMessage() throws IOException {
        sendMessage(sampleMessage);
    }


    private void sendMessage(Message message) throws IOException {
        channel.basicPublish("", queue, messageProperties(message), message.getBody().getBytes());
    }

    private AMQP.BasicProperties messageProperties(Message message) {
        return new AMQP.BasicProperties.Builder()
                .appId(message.getAppId())
                .userId(message.getUserId())
                .contentType(message.getContentType())
                .build();
    }
}
