package server.utils;

import java.io.File;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * @author andrzej.salamon@gmail.com
 *
 */

public class FileUtils {
    public static final String FILE_SEPARATOR = getFileseparator();

    public static String getFileseparator() {
        return System.getProperty("file.separator");
    }
}
