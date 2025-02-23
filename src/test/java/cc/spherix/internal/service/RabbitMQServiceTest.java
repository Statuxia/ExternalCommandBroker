package cc.spherix.internal.service;

import cc.spherix.internal.config.Configuration;
import cc.spherix.internal.config.ConfigurationManager;
import cc.spherix.internal.dto.MessageDTO;
import cc.spherix.internal.util.TestUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@Disabled(value = "need manual test")
class RabbitMQServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void testConnection() throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
                                 NoSuchMethodException, TimeoutException {
        final ConfigurationManager<Configuration> manager = TestUtil.initManager();
        final RabbitMQService service = RabbitMQService.of(manager.getConfiguration().getRabbitMQ());

        service.connect();
        assertTrue(service.isConnected());
        service.consume();
        assertTrue(service.isConsumed());

        service.stop();
        assertFalse(service.isConsumed());
        assertFalse(service.isConnected());
    }

    @Test
    void testReceive() throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
                              NoSuchMethodException, TimeoutException, InterruptedException {
        final UUID token = UUID.randomUUID();
        final String message = "Hello World!";

        final ConfigurationManager<Configuration> manager = TestUtil.initManager();
        final RabbitMQService service = RabbitMQService.of(manager.getConfiguration().getRabbitMQ());

        service.connect();
        assertTrue(service.isConnected());
        service.consume();
        assertTrue(service.isConsumed());


        sendMessage(service, manager, token, message);

        receiveMessage(service, message, token);
    }

    protected void sendMessage(
        RabbitMQService service, ConfigurationManager<Configuration> manager, UUID token, String message
    ) throws IOException, TimeoutException {
        try (Connection sendConnection = service.factory.newConnection();
             Channel sendChannel = sendConnection.createChannel()) {
            final String queueName = manager.getConfiguration().getRabbitMQ().getQueueName();

            final AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .headers(Map.of("token", token.toString()))
                .build();

            sendChannel.basicPublish(
                "",
                queueName,
                props,
                message.getBytes(StandardCharsets.UTF_8)
            );
            logger.info("token:{} message:{}", token, message);
        }
    }

    protected void receiveMessage(RabbitMQService service, String message, UUID token) throws InterruptedException {
        logger.info("Start receiving");
        for (int i = 0; i < 5; i++) {
            Thread.sleep(5000);

            final List<MessageDTO> messages = service.getBatch();
            if (messages.isEmpty()) {
                logger.info("Queue is empty");
                continue;
            }

            for (MessageDTO dto : messages) {
                logger.info("[user-{}] {}", dto.token(), dto.message());
                assertEquals(message, dto.message());
                assertEquals(token, dto.token());
            }

            try {
                logger.info("Stopping service");
                service.stop();
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
            assertFalse(service.isConsumed());
            assertFalse(service.isConnected());
            return;
        }

        logger.info("Too many tries. Nothing found");
        try {
            logger.info("Stopping service");
            service.stop();
            fail();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
