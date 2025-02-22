package cc.spherix.internal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Предоставляет инструменты для CRU<s>D</s> операций с конфигурационным файлом
 */
public final class ConfigurationManager<T> {

    private final Path path;
    private final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);
    private final Class<T> clazz;

    private T configuration;

    private ConfigurationManager(@NonNull Path path, Class<T> clazz)
        throws IOException, InstantiationException, IllegalAccessException,
               InvocationTargetException, NoSuchMethodException {
        this.path = path;
        this.clazz = clazz;

        createFile();
        readFile();
        updateFile();
    }

    public T getConfiguration() {
        return configuration;
    }

    /**
     * Создает директории и файл для конфигурации
     */
    private void createFile() throws IOException {
        if (!Files.isDirectory(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
    }

    /**
     * Обновление файла конфигурации
     */
    private void updateFile() throws IOException {
        mapper.writeValue(path.toFile(), configuration);
    }

    /**
     * Мягкое чтение файла с игнором ошибок при чтении<br>
     * Основная мотивация - обновление файла конфигурации новыми полями и исправление сломанного конфига
     */
    private void readFile()
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        T instance = getInstance(clazz);

        try {
            final T value = mapper.readValue(this.path.toFile(), clazz);

            instance = mapper.updateValue(instance, value);
        } catch (IOException ignore) {
            // ignore
        }

        this.configuration = instance;
    }

    /**
     * Создание объекта класса конфигурации<br>
     * Требования: все поля должны быть проинициализированы дефолтными значениями для обновления файла конфигурации
     */
    private T getInstance(Class<T> clazz)
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);

        return constructor.newInstance();
    }

    public static <T> ConfigurationManager<T> of(String path, Class<T> clazz)
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
               NoSuchMethodException {
        return new ConfigurationManager<>(Paths.get(path), clazz);
    }

    public static <T> ConfigurationManager<T> of(Path path, Class<T> clazz)
        throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException,
               NoSuchMethodException {
        return new ConfigurationManager<>(path, clazz);
    }
}
