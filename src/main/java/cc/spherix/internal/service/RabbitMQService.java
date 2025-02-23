package cc.spherix.internal.service;

import cc.spherix.internal.config.RabbitMQConfiguration;
import cc.spherix.internal.dto.MessageDTO;
import com.rabbitmq.client.*;
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
    protected boolean channelCreated;

    private RabbitMQService(@NonNull RabbitMQConfiguration configuration) {
        this.configuration = configuration;

        factory = new ConnectionFactory();
        factory.setHost(configuration.getHost());
        factory.setUsername(configuration.getUsername());
        factory.setPassword(configuration.getPassword());
        factory.setPort(configuration.getPort());
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
        connected = true;
    }

    /**
     * Настраиваем канал<br>
     * Для использования нужны права на конфигурацию
     */
    public void channel() throws IOException {
        if (channelCreated) {
            return;
        }
        channel.queueDeclare(
            configuration.getQueueName(),
            configuration.isDurable(),
            false,
            false,
            null
        );
        channelCreated = true;
    }

    /**
     * Отправка сообщения
     */
    public void send(String message, AMQP.BasicProperties props) throws IOException {
        channel.basicPublish(
            "amq.default".equals(configuration.getExchange()) ? "" : configuration.getExchange(),
            configuration.getQueueName(),
            props,
            message.getBytes()
        );
    }

    /**
     * Отправка сообщения без пропертей
     */
    public void send(String message) throws IOException {
        send(message, (AMQP.BasicProperties) null);
    }


    /**
     * Отправка сообщения с хедерами
     */
    public void send(String message, Map<String, Object> headers) throws IOException {
        send(message, new AMQP.BasicProperties.Builder().headers(headers).build());
    }


    /**
     * Подписываемся на получение сообщений по названию очереди
     */
    public void consume() throws IOException {
        if (!connected || consumed) {
            return;
        }

        final DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            final String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

            final Map<String, Object> headers = delivery.getProperties().getHeaders();
            final Object token = headers.getOrDefault("token", null);

            if (token == null) {
                return;
            }

            final UUID uuid;
            try {
                uuid = UUID.fromString(token.toString());
            } catch (Exception e) {
                return;
            }

            queue.add(new MessageDTO(message, uuid));
        };

        channel.basicConsume(configuration.getQueueName(), false, deliverCallback, consumerTag -> {
        });
        consumed = true;
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
     * Получение партии сообщений из очереди<br>
     * Лимит сообщений определяется извне, но не может быть больше батча
     *
     * @see RabbitMQConfiguration#getBatchSize()
     */
    public List<MessageDTO> getLimit(int limit) {
        final int size = Math.min(Math.min(queue.size(), limit), configuration.getBatchSize());
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
            consumed = false;
        }
        if (connection != null) {
            connection.close();
            connected = false;
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

    public boolean isConnected() {
        return connected;
    }

    public boolean isConsumed() {
        return consumed;
    }
}
