package isuret.polos.aopi.entities;

import com.badlogic.gdx.math.Rectangle;

/**
 * Interaction entity, reacting to user input.
 */
public interface IEntity extends IGraphic {

    /**
     * Get the bounding rectangle of the entity
     */
    Rectangle getBounds();

    void setDeviceName(String deviceName);

    String getDeviceName();

    void setAction(String action);

    String getAction();

    void setMouseOver(boolean mouseOver);

    boolean isMouseOver();
}
