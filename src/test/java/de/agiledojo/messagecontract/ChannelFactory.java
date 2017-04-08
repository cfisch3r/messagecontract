package de.agiledojo.messagecontract;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class ChannelFactory {

    private List<Channel> openChannels = new ArrayList<>();


    ConnectionFactory connectionFactory;

    public ChannelFactory(String rabbitMQHost, int rabbitMQPort) {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitMQHost);
        connectionFactory.setPort(rabbitMQPort);
    }

    public Channel createChannel() {
        try {
            Channel channel = connectionFactory.newConnection().createChannel();
            openChannels.add(channel);
            return channel;
        } catch (IOException|TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseAllChannels() {
        openChannels.forEach(channel -> {
            try {
                channel.close();
            } catch (IOException|TimeoutException e) {
                throw new RuntimeException(e);
        }});
        openChannels.clear();
    }
}
