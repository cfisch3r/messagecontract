package de.agiledojo.messagecontract;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.function.Consumer;

public class QueueAdapter {

    private final Channel channel;
    private final String queueName;

    public QueueAdapter(Channel channel, String queueName) {
        this.channel = channel;
        this.queueName = queueName;
    }

    public void onMessage(Consumer<Message> messageHandler) throws IOException {
        boolean autoAck = true;
        channel.basicConsume(queueName, autoAck, "myConsumerTag",
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException
                    {
                        messageHandler.accept(new Message(properties.getUserId(),
                                properties.getAppId(),
                                properties.getContentType(),
                                properties.getContentEncoding(),
                                properties.getTimestamp(),
                                new String(body, properties.getContentEncoding())));
                    }
                });
    }
}
