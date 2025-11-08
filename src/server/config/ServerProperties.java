package server.config;
import java.util.*;

public class ServerProperties extends Properties {
    private static final long serialVersionUID = 1L;

//    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
//
//    public static ResourceBundle getBundle(String baseName) {
//        return bundle;
//    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return super.put(key, value);
    }

    @Override
    public synchronized Object get(Object key) {
        return super.get(key);
    }

    public static int getConfigValueAsInt(String val) {
        return ServerPropertiesValue.getConfigValueAsInt(val);
    }

}