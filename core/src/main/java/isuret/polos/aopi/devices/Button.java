package isuret.polos.aopi.devices;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class Button extends DefaultEntity {

    @Override
    public void render(SpriteBatch batch, BitmapFont font, Vector3 mouse) {

        if (imageOn != null) {
            float scale = 30f / imageOn.getTexture().getWidth();
            float newHeight = imageOn.getTexture().getHeight() * scale;
            batch.draw(imageOn.getTexture(), imageOn.getX(), imageOn.getY(), 30, newHeight);
            font.draw(batch, text, bounds.x + (imageOn.getTexture().getWidth() / 2), bounds.y + (font.getLineHeight()/1.33f));
        } else {
            font.draw(batch, text, bounds.x + 10, bounds.y + (font.getLineHeight()/1.33f));
        }
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, Vector3 mouse) {

        if (imageOn == null) {
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


}
