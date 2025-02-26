package cc.spherix.internal.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {

    protected RabbitMQConfiguration rabbitMQ = new RabbitMQConfiguration();
    protected HikariCPConfiguration hikariCP = new HikariCPConfiguration();

    public RabbitMQConfiguration getRabbitMQ() {
        return rabbitMQ;
    }

    public void setRabbitMQ(RabbitMQConfiguration rabbitMQ) {
        this.rabbitMQ = rabbitMQ;
    }

    public HikariCPConfiguration getHikariCP() {
        return hikariCP;
    }

    public void setHikariCP(HikariCPConfiguration hikariCP) {
        this.hikariCP = hikariCP;
    }
}
