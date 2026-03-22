package isuret.polos.aopi.devices;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import isuret.polos.aopi.entities.IEntity;
import isuret.polos.aopi.entities.IGraphic;
import isuret.polos.aopi.entities.Image;

public class Button implements IEntity, IGraphic {

    private String action;
    private String deviceName;
    private Image imageOff;
    private Image imageOn;
    private boolean isOn = false;
    private Rectangle bounds;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Image getImageOff() {
        return imageOff;
    }

    public void setImageOff(Image imageOff) {
        this.imageOff = imageOff;
    }

    public Image getImageOn() {
        return imageOn;
    }

    public void setImageOn(Image imageOn) {
        this.imageOn = imageOn;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public void render(SpriteBatch batch) {

    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {

    }
}
