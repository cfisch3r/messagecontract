package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;
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
    private RabbitMQClient client;

    class MessageHandlerMock implements Consumer<Message> {

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

    private static ChannelFactory channelFactory;

    private MessageBrokerDouble messageBroker;

    @BeforeClass
    public static void setUpChannelFactory() throws Exception {
        channelFactory = new ChannelFactory(rabbitRule.getDockerHost(), rabbitRule.getHostPort("5672/tcp"));
    }

    @Before
    public void setUp() throws Exception {
        messageBroker = new MessageBrokerDouble(channelFactory.createChannel(), QUEUE_NAME, ERROR_QUEUE_NAME);
        queueAdapter = new QueueAdapter(channelFactory.createChannel(),QUEUE_NAME);
        client = new RabbitMQClient(channelFactory.createChannel());
    }

    @After
    public void tearDown() throws Exception {
        channelFactory.releaseAllChannels();
    }

    @Test
    public void messageHandlerIsCalledWhenMessageIsSent() throws InterruptedException, IOException {
        MessageHandlerMock messageHandlerMock = new MessageHandlerMock();
        queueAdapter.onMessage(messageHandlerMock);
        messageBroker.triggerMessage(sampleMessage);
        synchronized (messageHandlerMock) {
            messageHandlerMock.wait(TIMEOUT);
        }
        assertThat(messageHandlerMock.getLastAcceptedMessage()).isPresent().contains(sampleMessage);

    }

    @Test
    public void messageIsForwardedToErrorQueueAfterFailure() throws IOException, InterruptedException {
        queueAdapter.onMessage(new FailureMessageHandler());
        messageBroker.triggerMessage(sampleMessage);
        assertThat(client.pollMessageFromQueue(ERROR_QUEUE_NAME)).isPresent();
    }

}
