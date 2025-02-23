package cc.spherix.internal;

import cc.spherix.internal.config.Configuration;
import cc.spherix.internal.config.ConfigurationManager;
import cc.spherix.internal.dto.MessageDTO;
import cc.spherix.internal.service.RabbitMQService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

final class InternalRabbitMQApplication {

    private static ConfigurationManager<Configuration> configurationManager;
    private static RabbitMQService service;

    /**
     * Ручная точка запуска программы. Используется исключительно для тестирования.
     */
    public static void main(String[] args)
        throws IOException, InvocationTargetException, InstantiationException,
               IllegalAccessException, NoSuchMethodException, TimeoutException {
        configurationManager = initManager();
        service = RabbitMQService.of(getConfiguration().getRabbitMQ());
        service.connect();
        service.consume();

        final Timer timer = new Timer();

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                final List<MessageDTO> messages = service.getBatch();
                for (MessageDTO message : messages) {
                    System.out.printf("[user-%s] %s%n", message.token(), message.message());
                }
            }
        };

        timer.scheduleAtFixedRate(task, 2000, 1000);
    }

    private static Configuration getConfiguration() {
        return configurationManager.getConfiguration();
    }

    private static ConfigurationManager<Configuration> initManager()
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
               NoSuchMethodException {
        final Path path = Path.of(System.getProperty("user.dir"), ".env");
        return ConfigurationManager.of(
            Path.of(path.toString(), "config.json"), Configuration.class
        );
    }

    private InternalRabbitMQApplication() {
    }
}
