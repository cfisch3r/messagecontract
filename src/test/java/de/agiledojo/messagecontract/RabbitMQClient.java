package de.agiledojo.messagecontract;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Optional;

class RabbitMQClient {

    private static int POLL_TIMEOUT = 10000;

    private static int POLL_INTERVAL = 100;

    private Channel channel;


    RabbitMQClient(Channel channel) {
        this.channel = channel;
    }

    Optional<GetResponse> pollMessageFromQueue(String queueName) throws IOException, InterruptedException {
        Optional<GetResponse> response = getResponseFromQueue(queueName);
        if (response.isPresent())
            channel.basicAck(response.get().getEnvelope().getDeliveryTag(),false);
        return response;
    }

    GetResponse getMessageFromQueue(String queueName) throws IOException {
        return channel.basicGet(queueName, true);
    }

    void rejectMessageFromQueue(String queueName) throws IOException, InterruptedException {
        Optional<GetResponse> response = getResponseFromQueue(queueName);
        if (response.isPresent()) {
            channel.basicReject(response.get().getEnvelope().getDeliveryTag(), false);
        } else {
            throw new RuntimeException("Cannot fetch any message to reject.");
        }
   }

    private Optional<GetResponse> getResponseFromQueue(String queueName) throws IOException, InterruptedException {
        GetResponse message = null;
        for (int timer = 0; timer< POLL_TIMEOUT; timer += POLL_INTERVAL) {
            message = channel.basicGet(queueName, false);
            if (message == null)
                Thread.sleep(POLL_INTERVAL);
            else
                break;
        }
        return Optional.ofNullable(message);
    }

}
