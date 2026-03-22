package isuret.polos.aopi.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface IGraphic {

    /**
     * Render the entity, images, and more
     */
    void render(SpriteBatch batch);

    /**
     * Draw the entity, rectangles, and more
     */
    void draw(ShapeRenderer shapeRenderer);
}
