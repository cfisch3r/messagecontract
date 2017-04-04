package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Consumer;

public class QueueAdapterTest {

    public static final int TIMEOUT = 10000;

    boolean throwException = false;

    class MessageHandler implements Consumer<Message> {

        public Message message;

        @Override
        public void accept(Message message) {
            System.out.println(message.getBody());
            if (throwException) {
                throwException = false;
                throw new RuntimeException();
            }
            this.message = message;
            synchronized (this) {
                notifyAll(  );
            }
        }

        public void throwExceptionOnFirstCall() {
            throwException = true;
        }
    }

    private static final String QUEUE_NAME = "myQueue";
    private static final Message sampleMessage = new Message("guest","App ID","json",
            StandardCharsets.UTF_8.name(),new Date(120044000230L), "fsf");
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
        queue = new QueueDouble(channel, QUEUE_NAME, "errorQueue");
    }


    @Test
    public void messageHandlerIsCalledWhenMessageIsSent() throws InterruptedException, IOException {
        QueueAdapter adapter = new QueueAdapter(channel,QUEUE_NAME);
        MessageHandler messageHandler = new MessageHandler();


        adapter.onMessage(messageHandler);

        queue.sendMessage(sampleMessage);
        synchronized (messageHandler) {
            messageHandler.wait(TIMEOUT);
        }

        Assertions.assertThat(messageHandler.message).isEqualTo(sampleMessage);

    }

    @Test
    public void messageFetchesMessagesAfterException() throws IOException, InterruptedException {
        QueueAdapter adapter = new QueueAdapter(channel,QUEUE_NAME);
        MessageHandler messageHandler = new MessageHandler();
        messageHandler.throwExceptionOnFirstCall();


        adapter.onMessage(messageHandler);

        queue.sendMessage(sampleMessage);
        queue.sendMessage(createMessageWithBody("zwo"));

        synchronized (messageHandler) {
            messageHandler.wait(TIMEOUT);
            messageHandler.wait(TIMEOUT);
        }


        Assertions.assertThat(messageHandler.message).isEqualTo(sampleMessage);
    }

    private Message createMessageWithBody(String body) {
        return new Message("guest","App ID","json",
                StandardCharsets.UTF_8.name(),new Date(120044000230L), body);
    }
}
