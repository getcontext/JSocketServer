package server.config;

import server.core.Server;

public class ServerPropertiesValue {
    private static String getConfigValue(String val) {
        ServerProperties cfg = Server.getServerProperties();
        if (cfg == null) return "";
        String v = (String)cfg.get(val);
        return v == null ? "" : v;
    }

    @SuppressWarnings("unused")
    public static int getConfigValueAsInt(String val) {
        String v = getConfigValue(val);
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
//            Server.LOGGER.warning("NumberFormatException: " + e.getMessage());
            return -1;
        }
    }
}
