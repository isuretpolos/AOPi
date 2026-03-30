package isuret.polos.aopi.devices;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import isuret.polos.aopi.entities.IEntity;

/**
 * Represents a device that can be controlled by the user.
 */
public class Device implements IEntity {

    public String name;
    public String author;
    public String description;

    /**
     * The author's website
     */
    public String url;
    public String image;
    /**
     * The bounds of the device. Whatever you draw on the screen, and
     * it is inside this rectangle, it is considered to be part of this device.
     */
    public Rectangle bounds;
    public boolean mouseOver = false;

    public Device(String name, String author, String description, String url, String image, Rectangle bounds) {
        this.name = name;
        this.author = author;
        this.description = description;
        this.url = url;
        this.image = image;
        this.bounds = bounds;
    }

    public Device() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void setDeviceName(String deviceName) {
        // ---
    }

    @Override
    public String getDeviceName() {
        return name;
    }

    @Override
    public void setAction(String action) {
        // Drag?
    }

    @Override
    public String getAction() {
        return "";
    }

    @Override
    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    @Override
    public boolean isMouseOver() {
        return mouseOver;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    public void render(SpriteBatch batch, BitmapFont font, Vector3 mouse) {

    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, Vector3 mouse) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (isMouseOver()) {
            shapeRenderer.setColor(1, 0, 0, 1);
        } else {
            shapeRenderer.setColor(1, 1, 1, 1);
        }
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.end();
    }
}
