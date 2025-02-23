package cc.spherix.internal.util;

import cc.spherix.internal.config.Configuration;
import cc.spherix.internal.config.ConfigurationManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public class TestUtil {

    private TestUtil() {
    }

    public static ConfigurationManager<Configuration> initManager()
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
               NoSuchMethodException {
        final Path path = Path.of(System.getProperty("user.dir"), ".env");
        return ConfigurationManager.of(
            Path.of(path.toString(), "test_config.json"), Configuration.class
        );
    }
}
