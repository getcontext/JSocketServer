package server.utils;

import java.text.SimpleDateFormat;

import java.util.Date;


public class DateUtils {
    private static final SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String format(Date d) {
        return sf.format(d);
    }
}
