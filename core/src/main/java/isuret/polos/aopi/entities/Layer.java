package isuret.polos.aopi.entities;

import isuret.polos.aopi.Main;

import java.util.ArrayList;
import java.util.List;

public class Layer {

    private Main.LayerType layerType;
    private int zIndex;
    private boolean visible;
    private List<IEntity> entities = new ArrayList<>();

    public Layer(Main.LayerType layerType, int zIndex, boolean visible) {
        this.layerType = layerType;
        this.zIndex = zIndex;
        this.visible = visible;
    }

    public void addEntity(IEntity entity) {
        entities.add(entity);
    }

    public List<IEntity> getEntities() {
        return entities;
    }

    public String getName() {
        return layerType.name();
    }

    public int getZIndex() {
        return zIndex;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
