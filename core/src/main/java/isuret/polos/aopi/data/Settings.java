package isuret.polos.aopi.data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Settings {

    private LocalDateTime lastUpdate;
    private Map<String, Object> keyValues = new HashMap<>();

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Map<String, Object> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(Map<String, Object> keyValues) {
        this.keyValues = keyValues;
    }
}
