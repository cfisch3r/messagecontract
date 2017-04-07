package de.agiledojo.messagecontract;

import com.github.geowarin.junit.DockerRule;

class RabbitMQDockerRule {
    static DockerRule build() {
        return DockerRule.builder()
                .image("rabbitmq:latest")
                .ports("5672")
                .waitForPort("5672/tcp")
                .waitForLog("Server startup complete")
                .build();
    }
}
