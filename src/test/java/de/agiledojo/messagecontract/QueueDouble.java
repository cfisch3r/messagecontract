package de.agiledojo.messagecontract;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

class QueueDouble {

    private Channel channel;

    private String name;


    QueueDouble(Channel channel,String queueName) throws IOException, TimeoutException {
        this.channel = channel;
        channel.queueDeclare(queueName, false, false, false, null);
        name = queueName;
    }

    void sendMessage(Message message) throws IOException {
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
