package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import org.junit.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class QueueAdapterTest {

    public static final int TIMEOUT = 10000;

    public static final String ERROR_QUEUE_NAME = "errorQueue";

    private static final String QUEUE_NAME = "myQueue";

    private static final Message sampleMessage = new Message("guest","App ID","json",
            StandardCharsets.UTF_8.name(),new Date(120044000230L), "fsf");

    private QueueAdapter queueAdapter;

    class MessageHandler implements Consumer<Message> {

        private Message message;

        @Override
        public void accept(Message message) {
            this.message = message;
            synchronized (this) {
                 notifyAll();
            }
        }

        public Optional<Message> getLastAcceptedMessage() {
            return Optional.ofNullable(message);
        }
    }

    class FailureMessageHandler implements Consumer<Message> {

        @Override
        public void accept(Message message) {
            throw new RuntimeException();
        }
    }

    @ClassRule
    public static DockerRule rabbitRule = RabbitMQDockerRule.build();

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
        queue = new QueueDouble(channel, QUEUE_NAME, "errorQueue");
        queueAdapter = new QueueAdapter(channel,QUEUE_NAME);
    }

    @After
    public void tearDown() throws Exception {
        channel.close();
    }

    @Test
    public void messageHandlerIsCalledWhenMessageIsSent() throws InterruptedException, IOException {
        MessageHandler messageHandler = new MessageHandler();
        queueAdapter.onMessage(messageHandler);
        queue.sendMessage(sampleMessage);
        synchronized (messageHandler) {
            messageHandler.wait(TIMEOUT);
        }
        assertThat(messageHandler.getLastAcceptedMessage()).isPresent().contains(sampleMessage);

    }

    @Test
    public void messageFetchesMessagesAfterFailure() throws IOException, InterruptedException {
        queueAdapter.onMessage(new FailureMessageHandler());
        queue.sendMessage(sampleMessage);
        assertThat(tryToGetMessage(10, ERROR_QUEUE_NAME)).isPresent();
    }

    private Optional<GetResponse> tryToGetMessage(int timeout, String queueName) throws IOException, InterruptedException {
        GetResponse message;
        do {
            message = getMessageFromQueue(queueName);
            Thread.sleep(1000);
            timeout--;
        } while (message == null && timeout > 0);

        return Optional.ofNullable(message);
    }

    private GetResponse getMessageFromQueue(String queueName) throws IOException {
        return channel.basicGet(queueName, true);
    }
}
