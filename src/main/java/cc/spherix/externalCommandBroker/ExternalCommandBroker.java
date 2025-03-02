package cc.spherix.externalCommandBroker;

import cc.spherix.externalCommandBroker.util.Logger;
import cc.spherix.internal.config.Configuration;
import cc.spherix.internal.config.ConfigurationManager;
import cc.spherix.internal.dao.TokenDao;
import cc.spherix.internal.domain.Token;
import cc.spherix.internal.service.DataSourceService;
import cc.spherix.internal.service.RabbitMQService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public final class ExternalCommandBroker extends JavaPlugin {

    private static ConfigurationManager<Configuration> manager;
    private static RabbitMQService rabbitMQService;
    private static DataSourceService dataSourceService;

    private Set<String> validUuids = new HashSet<>();

    @Override
    public void onEnable() {
        prepare();

        Logger.init(this);

        /**
         * Get/Update valid uuids from database
         */
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            TokenDao.getInstance().getAllTokens().thenAccept(tokens -> {
                validUuids = tokens.stream().map(Token::token).collect(Collectors.toSet());
            });
        }, 0, 20 * 60L);

        /**
         * Execute commands
         */
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            rabbitMQService.getBatch().forEach(command -> {
                final UUID token = command.token();

                if (token == null || !validUuids.contains(token.toString())) {
                    return;
                }

                Logger.debug("[user #{}] executing command: `{}`", token, command);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.message());
            });
        }, 0, 20);
    }

    @Override
    public void onDisable() {
        RabbitMQService.stopAll();
        DataSourceService.stopAll();
    }

    public static ConfigurationManager<Configuration> initManager()
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
               NoSuchMethodException {
        final Path path = Path.of(
            Path.of(System.getProperty("user.dir"), "plugins").toString(), "spherix"
        );
        return ConfigurationManager.of(
            Path.of(path.toString(), "ExternalCommandBroker.json"), Configuration.class
        );
    }

    private static void prepare() {
        try {
            manager = initManager();
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException
                 | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        rabbitMQService = RabbitMQService.of(manager.getConfiguration().getRabbitMQ());
        dataSourceService = DataSourceService.of(manager.getConfiguration().getHikariCP());

        prepareRabbitMQService();
        prepareDatabase();
    }

    private static void prepareRabbitMQService() {
        try {
            rabbitMQService.connect();
            rabbitMQService.channel();
            rabbitMQService.consume();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private static void prepareDatabase() {
        try {
            if (dataSourceService.getConnection().isClosed()) {
                throw new RuntimeException("connection closed");
            }

            TokenDao.init(dataSourceService);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
