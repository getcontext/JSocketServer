package server.config;

import java.util.HashMap;

public class Config {
    private static HashMap<String, Config> config = new HashMap<String, Config>();

    protected Config() {
        Config.add("default", new Config());
    }

    public static Config create() {
        return new Config();
    }

    public static HashMap<String, Config> get() {
        return config;
    }

    public static void set(HashMap<String, Config> config) {
        Config.config = config;
    }

    public static void add(String key, Config config) {
        get().put(key, config);
    }
}
