package isuret.polos.aopi.entities;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public interface IGraphic {

    /**
     * Render the entity, images, and more
     */
    void render(SpriteBatch batch, BitmapFont font, Vector3 mouse);

    /**
     * Draw the entity, rectangles, and more
     */
    void draw(ShapeRenderer shapeRenderer, Vector3 mouse);
}
