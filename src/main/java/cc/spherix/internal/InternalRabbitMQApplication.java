package cc.spherix.internal;

import cc.spherix.internal.config.Configuration;
import cc.spherix.internal.config.ConfigurationManager;
import cc.spherix.internal.config.RabbitMQConfiguration;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

final class InternalRabbitMQApplication {

    private final ConfigurationManager<Configuration> configurationManager;

    private InternalRabbitMQApplication()
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
        NoSuchMethodException {
        this.configurationManager = initManager();
    }

    public static void main(String[] args)
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
        NoSuchMethodException {
        new InternalRabbitMQApplication().run();
    }

    public Configuration getConfiguration() {
        return configurationManager.getConfiguration();
    }

    private void run() {
        ConnectionFactory factory = new ConnectionFactory();
        final RabbitMQConfiguration rabbitMQ = getConfiguration().getRabbitMQ();

        factory.setHost(rabbitMQ.getHost());
        factory.setPassword(rabbitMQ.getPassword());
        factory.setPort(rabbitMQ.getPort());
        factory.setUsername(rabbitMQ.getUsername());
    }

    private ConfigurationManager<Configuration> initManager()
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
        NoSuchMethodException {
        final Path path = Path.of(System.getProperty("user.dir"), ".env");
        return ConfigurationManager.of(
            Path.of(path.toString(), "config.json"), Configuration.class
        );
    }
}
