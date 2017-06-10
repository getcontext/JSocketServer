package server.utils;

import java.io.File;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;


/**
 * klasa odpowiada za operacje na plikach oray katalogach
 *
 * @author andrzej.salamon@biztech.pl
 *
 */
public class FileUtils {
    /**
     * aktualny separator �cie�ki pliku, w oparciu o system na jakim uruchomiony
     * jest serwer
     */
    public static final String FileSeparator = getFileseparator();

    private static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
    private static File file;

    /**
     * zwraca aktualny separator �cie�ki pliku, w oparciu o system na jakim
     * uruchomiony jest serwer
     *
     * @return aktualny separator �cie�ki pliku
     */
    public static String getFileseparator() {
        return System.getProperty("file.separator");
    }

    /**
     * tworzy �cie�ke w lokalizacji docelowej , zgodnie ze standardem przyj�tym
     * w specyfikacji
     *
     * @param baseDir
     *            bazowy katalog g��wny
     * @param logDate
     *            data logowania(przenoszenia pliku)
     * @param logClass
     *            nazwa klasy logowania
     * @param machine
     *            nazwa maszyny z jakiej plik jest przenoszony
     * @return
     */
    public static String createDir(String baseDir, String logDate, String logClass, String machine) {
        // Date log = new Date(logDate);
        Date logDate_ = null;
        try {
            logDate_ = sf.parse(logDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final String logDateStr = sf.format(logDate_).toString();
        final String directory =
            baseDir + FileSeparator + logDateStr + FileSeparator + logClass + FileSeparator + machine + FileSeparator;

        if (isDir(directory)) {
            return directory;
        }

        file = new File(directory);

        if (file.mkdirs()) {
            return directory;    
        }
        return null;
    }

    /**
     * sprawdza czy podana nazwa jest katalogiem
     *
     * @param dir
     *            pe�na �cie�ka do katalogu
     * @return prawd� w przypadku, gdy podany parametr jest katalogiem, w
     *         przeciwnym wypadku fa�sz
     */
    public static boolean isDir(String dir) {
        file = new File(dir);
        return file.isDirectory();

    }

    /**
     * zwraca wygenerowan� nazw� pliku, w miejscu docelowym
     *
     * @param genDate
     *            data przenoszenia pliku w miejsce docelowe
     * @param machine
     *            nazwa maszyny z jakiej plik jest przenoszony
     * @param logClass
     *            nazwa klasy logu(typu pliku)
     * @return wygenerowana nazwa pliku w postaci tekstowej
     */
    public static String getName(Long genDate, String machine, String logClass) {        
        return sf.format(new Date(genDate)) + "_" + machine + "_" + logClass;
    }

    public static void deleteFile(String fileName) throws IllegalArgumentException {
        file = new File(fileName);

        // Make sure the file or directory exists and isn't write protected
        if (!file.exists())
            throw new IllegalArgumentException("Delete: no such file or directory: " + fileName);

        if (!file.canWrite())
            throw new IllegalArgumentException("Delete: write protected: " + fileName);

        // If it is a directory, make sure it is empty
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files.length > 0)
                throw new IllegalArgumentException("Delete: directory not empty: " + fileName);
        }

        // Attempt to delete it
        boolean success = file.delete();

        if (!success)
            throw new IllegalArgumentException("Delete: deletion failed");
    }

    // public static void main(String[] args) {
    // FileUtils.deleteFile("D:\\archiveconnector\\cache\\komp1\\eclipse.exe");
    // }
}
