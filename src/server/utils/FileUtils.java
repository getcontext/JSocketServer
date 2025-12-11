package server.utils;

import java.io.IOException;
import java.io.InputStream;

import server.config.ServerProperties;

/**
 * @author andrzej.salamon@gmail.com
 *
 */

public class FileUtils {
    /**
     * Singleton instance of {@link FileUtils}. The instance is created eagerly and is
     * therefore thread‑safe without requiring additional synchronization.
     */
    private static final FileUtils INSTANCE = new FileUtils();

    /**
     * Private constructor to prevent external instantiation. The class provides a
     * singleton instance via {@link #getInstance()}.
     */
    private FileUtils() {
        // Prevent instantiation
    }

    /**
     * Returns the singleton instance of {@link FileUtils}.
     *
     * @return the singleton {@code FileUtils} instance
     */
    public static FileUtils getInstance() {
        return INSTANCE;
    }

    public static final String FILE_SEPARATOR = getFileseparator();

    public static String getFileseparator() {
        return System.getProperty("file.separator");
    }

    public static ServerProperties loadServerProperties(String fileName) throws IOException {
        // First try to load from the file system using the supplied path.
        ServerProperties properties = new ServerProperties();
        ClassLoader loader = FileUtils.class.getClassLoader();

        InputStream stream = loader.getResourceAsStream(fileName);
        if (stream == null) {
            // If the resource is not found, mimic the previous behavior by throwing NoSuchFileException
            throw new IOException(fileName);
        }
        properties.load(stream);
        return properties;
    }
}
