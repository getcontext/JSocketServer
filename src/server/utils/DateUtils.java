package server.utils;

import java.text.SimpleDateFormat;

import java.util.Date;


/**
 * klasa pomocnicza odpowiadaj�ca za operacja na datach
 *
 * @author andrzej.salamon@biztech.pl
 *
 */
public class DateUtils {
    private static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * formatuje wybran� date do formatu odpowiadaj�cego systemowi
     *
     * @param d
     *            obiekt daty
     * @return sformatowan� date w formie tekstu
     */
    public static String format(Date d) {
//        String formatDate = sf.format(d);
        return sf.format(d);
    }
}
