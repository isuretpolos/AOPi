package isuret.polos.aopi.data;

import tools.jackson.databind.ObjectMapper;

import java.io.File;

public class Database {

    private static Database instance;
    private final File file;
    private ObjectMapper objectMapper;
    private Settings settings = new Settings();

    private Database() {
        objectMapper = new ObjectMapper();
        file = new File("settings.json");
        if (file.exists()) {
            System.out.println("Settings file found, loading...");
            settings = objectMapper.readValue(file, Settings.class);
        }
    }

    private Settings getSettings() {
        return settings;
    }

    private void saveSettings() {
        objectMapper.writeValue(file, getSettings());
    }

    private static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public static void setValue(String key, Object value) {
        getInstance().getSettings().getKeyValues().put(key, value);
    }

    public static Object getValue(String key) {
        return getInstance().getSettings().getKeyValues().get(key);
    }

    public static void save() {
        getInstance().getSettings().setLastUpdate(java.time.LocalDateTime.now());
        getInstance().saveSettings();
    }
}
