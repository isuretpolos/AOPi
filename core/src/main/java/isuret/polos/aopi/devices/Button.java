package isuret.polos.aopi.devices;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import isuret.polos.aopi.entities.IEntity;
import isuret.polos.aopi.entities.Image;

public class Button extends DefaultEntity {

    @Override
    public void render(SpriteBatch batch, BitmapFont font, Vector3 mouse) {
        font.draw(batch, text, bounds.x + 10, bounds.y + (font.getLineHeight()/1.33f));
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, Vector3 mouse) {
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }


}
