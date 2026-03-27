package isuret.polos.aopi.devices;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import isuret.polos.aopi.entities.IEntity;
import isuret.polos.aopi.entities.Image;

public class Button implements IEntity {

    private String action;
    private String deviceName;
    private String text;
    private Image imageOff;
    private Image imageOn;
    private boolean isOn = false;
    private Rectangle bounds;
    private boolean mouseOver = false;

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
    public void render(SpriteBatch batch, BitmapFont font, Vector3 mouse) {
        font.draw(batch, text, bounds.x + 10, bounds.y + (font.getLineHeight()/1.33f));
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, Vector3 mouse) {
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
