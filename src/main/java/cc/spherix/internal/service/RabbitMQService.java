package cc.spherix.internal.service;

import cc.spherix.internal.config.RabbitMQConfiguration;
import cc.spherix.internal.dto.MessageDTO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

@Singleton
public class RabbitMQService {

    private static final Map<RabbitMQConfiguration, RabbitMQService> MQ_SERVICES = new HashMap<>();

    protected RabbitMQConfiguration configuration;
    protected final ConnectionFactory factory;
    protected Connection connection;
    protected Channel channel;

    private final Queue<MessageDTO> queue = new ConcurrentLinkedQueue<>();
    protected boolean connected;
    protected boolean consumed;

    private RabbitMQService(@NonNull RabbitMQConfiguration configuration) {
        this.configuration = configuration;

        factory = new ConnectionFactory();
        factory.setHost(configuration.getHost());
        factory.setUsername(configuration.getUsername());
        factory.setPassword(configuration.getPassword());
    }

    /**
     * Коннектимся к реббиту и открываем канал
     */
    public void connect() throws IOException, TimeoutException {
        if (connected) {
            return;
        }
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(
            configuration.getQueueName(),
            configuration.isDurable(),
            false,
            false,
            null
        );
        connected = true;
    }

    /**
     * Подписываемся на получение сообщений по названию очереди
     */
    public void consume() throws IOException {
        if (consumed) {
            return;
        }

        final DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            final String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

            final Map<String, Object> headers = delivery.getProperties().getHeaders();
            final Object token = headers.get("token");

            final UUID uuid;
            switch (token) {
                case UUID u -> uuid = u;
                case String s -> {
                    try {
                        uuid = UUID.fromString(s);
                    } catch (Exception ignored) {
                        return;
                    }
                }
                default -> {
                    return;
                }
            }

            queue.add(new MessageDTO(message, uuid));
        };

        channel.basicConsume(configuration.getQueueName(), false, deliverCallback, consumerTag -> {
        });
    }

    /**
     * Получение первого сообщения из очереди
     */
    @Nullable
    public MessageDTO get() {
        return queue.poll();
    }

    /**
     * Получение партии сообщений из очереди<br>
     * Лимит сообщений определяется параметром в конфигурации
     *
     * @see RabbitMQConfiguration#getBatchSize()
     */
    public List<MessageDTO> getBatch() {
        final int size = Math.min(queue.size(), configuration.getBatchSize());
        final List<MessageDTO> messages = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (queue.isEmpty()) {
                return messages;
            }
            messages.add(queue.poll());
        }

        return messages;
    }

    /**
     * Получение всех сообщений. В ходе сбора дополнительно учитываем нештатное изменение очереди
     */
    public List<MessageDTO> getAll() {
        final int size = queue.size();
        final List<MessageDTO> messages = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (queue.isEmpty()) {
                return messages;
            }
            messages.add(queue.poll());
        }

        return messages;
    }

    /**
     * Закрываем коннекты и канал
     */
    public void stop() throws IOException, TimeoutException {
        if (channel != null) {
            channel.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    public static void stopAll() {
        MQ_SERVICES.forEach((config, service) -> {
            try {
                service.stop();
            } catch (IOException | TimeoutException e) {
                // ignored
            }
        });
    }

    public static RabbitMQService of(RabbitMQConfiguration configuration) {
        return MQ_SERVICES.computeIfAbsent(configuration, RabbitMQService::new);
    }
}
