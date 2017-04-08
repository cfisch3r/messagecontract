package de.agiledojo.messagecontract;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

class MessageBrokerDouble {

    private Channel channel;

    private String name;

    MessageBrokerDouble(Channel channel, String queueName, String errorQueueName) throws IOException, TimeoutException {
        this.channel = channel;
        channel.exchangeDeclare("errorExchange", "direct");
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "errorExchange");
        args.put("x-dead-letter-routing-key", errorQueueName);
        channel.queueDeclare(queueName, false, false, false, args);
        channel.queueDeclare(errorQueueName, false, false, false, null);
        channel.queueBind(errorQueueName,"errorExchange",errorQueueName);
        name = queueName;
    }

    void triggerMessage(Message message) throws IOException {
        channel.basicPublish("", name, messageProperties(message), message.getBody().getBytes());
    }

    private AMQP.BasicProperties messageProperties(Message message) {
        return new AMQP.BasicProperties.Builder()
                .appId(message.getAppId())
                .userId(message.getUserId())
                .contentType(message.getContentType())
                .timestamp(message.getTimeStamp())
                .contentEncoding(message.getContentEncoding())
                .build();
    }
}
