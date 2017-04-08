package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;
import com.rabbitmq.client.*;
import org.junit.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageBrokerDoubleTest {

    private static final String ERROR_QUEUE_NAME = "errorQueue";
    private static final String QUEUE_NAME = "myQueue";
    private static final Message sampleMessage = new Message("guest","App ID","json",
            StandardCharsets.UTF_8.name(),new Date(120044000230L), "fsf");
    private static ChannelFactory channelFactory;

    @ClassRule
    public static DockerRule rabbitRule = RabbitMQDockerRule.build();
    private MessageBrokerDouble messageBroker;
    private RabbitMQClient client;

    @BeforeClass
    public static void setUpChannelFactory() throws Exception {
        channelFactory = new ChannelFactory(rabbitRule.getDockerHost(),rabbitRule.getHostPort("5672/tcp"));
    }

    @Before
    public void setUp() throws Exception {
        messageBroker = new MessageBrokerDouble(channelFactory.createChannel(), QUEUE_NAME, ERROR_QUEUE_NAME);
        client = new RabbitMQClient(channelFactory.createChannel());
    }

    @After
    public void tearDown() throws Exception {
        channelFactory.releaseAllChannels();
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
        assertThat(client.getMessageFromQueue(ERROR_QUEUE_NAME)).isNull();
    }

    @Test
    public void rejectedMessageHasAReasonHeader() throws IOException, InterruptedException {
        sendSampleMessage();
        rejectMessageFromQueue();
        assertThat(getLastDeathHeaderValue(getMessageFromQueue(ERROR_QUEUE_NAME), "reason")).isEqualTo("rejected");
    }

    private String getLastDeathHeaderValue(GetResponse message, String header) throws IOException {
        Map<String, Object> lastHeader = getLastDeathHeadersFromMessage(message);
        return lastHeader.get(header).toString();
    }

    private Map<String, Object> getLastDeathHeadersFromMessage(GetResponse rejectedMessage) {
        List<Map<String,Object>> deathHeaders = (List<Map<String,Object>>) rejectedMessage.getProps().getHeaders().get("x-death");
        return deathHeaders.get(deathHeaders.size() - 1);
    }

    private void rejectMessageFromQueue() throws IOException, InterruptedException {
        client.rejectMessageFromQueue(QUEUE_NAME);
    }
//
    private void sendSampleMessage() throws IOException {
        messageBroker.triggerMessage(sampleMessage);
    }

    private GetResponse getMessageFromQueue() throws IOException {
        try {
            return client.pollMessageFromQueue(QUEUE_NAME).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private GetResponse getMessageFromQueue(String queueName) throws IOException {
        try {
            return client.pollMessageFromQueue(queueName).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }    }
 }
