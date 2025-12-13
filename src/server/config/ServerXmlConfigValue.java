package server.config;

import server.core.Server;

@Deprecated
public class ServerXmlConfigValue {
    private static String getConfigValue(String val) {
        XmlServerConfig cfg = Server.getXmlConfig();
        if (cfg == null) return "";
        String v = cfg.get(val);
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
