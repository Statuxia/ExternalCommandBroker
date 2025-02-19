package cc.spherix.internal.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {

    protected RabbitMQConfiguration rabbitMQ = new RabbitMQConfiguration();

    public RabbitMQConfiguration getRabbitMQ() {
        return rabbitMQ;
    }

    public void setRabbitMQ(RabbitMQConfiguration rabbitMQ) {
        this.rabbitMQ = rabbitMQ;
    }
}
