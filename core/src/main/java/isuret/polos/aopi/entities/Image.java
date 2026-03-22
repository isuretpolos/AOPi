package isuret.polos.aopi.entities;

import com.badlogic.gdx.graphics.Texture;

public class Image extends Position {

    private String path;
    private Texture texture;

    public Image(Texture texture, int x, int y) {
        super(x, y);
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }
}
