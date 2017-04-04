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

    private static final String QUEUE_NAME = "myQueue";
    private static final Message sampleMessage = new Message("guest","App ID","json",
            StandardCharsets.UTF_8.name(),new Date(120044000230L), "fsf");
    public static final String ERROR_QUEUE_NAME = "errorQueue";
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
    private QueueDouble queue;
    private Connection connection;

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
        queue = new QueueDouble(channel, QUEUE_NAME, ERROR_QUEUE_NAME);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
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

    @Test
    public void messageHasBody() throws IOException {
        sendSampleMessage();
        GetResponse response = getMessageFromQueue();
        assertThat(response.getBody()).isEqualTo(sampleMessage.getBody().getBytes());
    }

    @Test
    public void messageHasTimeStamp() throws IOException {
        sendSampleMessage();
        GetResponse response = getMessageFromQueue();
        assertThat(response.getProps().getTimestamp()).isEqualToIgnoringMillis(sampleMessage.getTimeStamp());
    }

    @Test
    public void messageHasContentEncoding() throws IOException {
        sendSampleMessage();
        GetResponse response = getMessageFromQueue();
        assertThat(response.getProps().getContentEncoding()).isEqualTo(sampleMessage.getContentEncoding());
    }

    @Test
    public void errorQueueIsEmpty() throws IOException {
        assertThat(getMessageFromErrorQueue()).isNull();
    }

    @Test
    public void errorQueueHasMessageWhenMessageWasRejected() throws IOException {
        sendSampleMessage();
        GetResponse gr = channel.basicGet(QUEUE_NAME, false);
        channel.basicReject(gr.getEnvelope().getDeliveryTag(),false);
        channel.queuePurge(QUEUE_NAME);
        assertThat(getMessageFromErrorQueue()).isNotNull();
    }

    private GetResponse getMessageFromErrorQueue() throws IOException {
        return getMessageFromQueue(ERROR_QUEUE_NAME);
    }

    private void sendSampleMessage() throws IOException {
        queue.sendMessage(sampleMessage);
    }

    private GetResponse getMessageFromQueue() throws IOException {
        return getMessageFromQueue(QUEUE_NAME);
    }

    private GetResponse getMessageFromQueue(String queueName) throws IOException {
        return channel.basicGet(queueName, true);
    }
 }
