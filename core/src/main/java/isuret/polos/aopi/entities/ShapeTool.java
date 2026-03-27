package isuret.polos.aopi.entities;

import com.badlogic.gdx.math.Rectangle;

public class ShapeTool {

    public static Rectangle createNormalizedRectangle(float x1, float y1, float x2, float y2) {
        float x = Math.min(x1, x2);
        float y = Math.min(y1, y2);
        float width = Math.abs(x2 - x1);
        float height = Math.abs(y2 - y1);
        return new Rectangle(x, y, width, height);
    }
}
