package cc.spherix.internal.service;

import cc.spherix.internal.config.Configuration;
import cc.spherix.internal.config.ConfigurationManager;
import cc.spherix.internal.util.TestUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Disabled(value = "need manual test")
class DataSourceServiceTest {

    @Test
    void testConnection()
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
               NoSuchMethodException, SQLException {
        final ConfigurationManager<Configuration> manager = TestUtil.initManager();
        final DataSourceService service = DataSourceService.of(manager.getConfiguration().getHikariCP());

        assertFalse(service.getConnection().isClosed());

        service.stop();
    }
}
