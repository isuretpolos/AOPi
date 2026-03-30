package isuret.polos.aopi.devices;

import com.badlogic.gdx.math.Rectangle;
import isuret.polos.aopi.entities.IEntity;
import isuret.polos.aopi.entities.Image;

public abstract class DefaultEntity implements IEntity {

    protected String action;
    protected String deviceName;
    protected String text;
    protected Image imageOff;
    protected Image imageOn;
    protected boolean isOn = false;
    protected Rectangle bounds;
    protected boolean mouseOver = false;

    public String getAction() {
        return action;
    }

    @Override
    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    @Override
    public boolean isMouseOver() {
        return mouseOver;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }
}
