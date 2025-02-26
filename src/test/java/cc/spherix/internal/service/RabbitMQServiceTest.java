package cc.spherix.internal.service;

import cc.spherix.internal.config.Configuration;
import cc.spherix.internal.config.ConfigurationManager;
import cc.spherix.internal.dto.MessageDTO;
import cc.spherix.internal.util.TestUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@Disabled(value = "need manual test")
class RabbitMQServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void testConnection()
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
               NoSuchMethodException, TimeoutException {
        final ConfigurationManager<Configuration> manager = TestUtil.initManager();
        final RabbitMQService service = RabbitMQService.of(manager.getConfiguration().getRabbitMQ());

        service.connect();
        assertTrue(service.isConnected());

        service.stop();
        assertFalse(service.isConnected());
    }

    @Test
    void testChannel()
        throws IOException, TimeoutException, InvocationTargetException, InstantiationException,
               IllegalAccessException, NoSuchMethodException {
        final ConfigurationManager<Configuration> manager = TestUtil.initManager();
        final RabbitMQService service = RabbitMQService.of(manager.getConfiguration().getRabbitMQ());

        service.connect();

        assertDoesNotThrow(service::channel);

        service.stop();
    }

    @RepeatedTest(3)
    void testSend()
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
               NoSuchMethodException, TimeoutException {
        final UUID token = UUID.randomUUID();
        final String message = "Lorem ipsum " + ThreadLocalRandom.current().nextLong(10000, 99999);

        final ConfigurationManager<Configuration> manager = TestUtil.initManager();
        final RabbitMQService service = RabbitMQService.of(manager.getConfiguration().getRabbitMQ());

        service.connect();

        assertDoesNotThrow(() -> service.send(message, Map.of("token", token.toString())));

        service.stop();
    }

    @Test
    void testReceive()
        throws IOException, TimeoutException, InvocationTargetException, InstantiationException,
               IllegalAccessException, NoSuchMethodException, InterruptedException {
        final ConfigurationManager<Configuration> manager = TestUtil.initManager();
        final RabbitMQService service = RabbitMQService.of(manager.getConfiguration().getRabbitMQ());

        service.connect();
        service.consume();

        Thread.sleep(2000);

        final List<MessageDTO> messages = service.getAll();

        assertFalse(messages.isEmpty());

        for (MessageDTO dto : messages) {
            logger.info("[user-{}] {}", dto.token(), dto.message());
        }

        service.stop();
    }
}
