package isuret.polos.aopi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import isuret.polos.aopi.entities.Image;
import isuret.polos.aopi.input.InputProcessorAOPi;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    public enum Mode {
        DRAW, TEXT
    }
    public Mode selectedMode;
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;
    public GlyphLayout layout = new GlyphLayout();
    public boolean drawing = false;
    public List<Image> images = new ArrayList<>();
    public List<Rectangle> rectangles = new ArrayList<>();
    public float touchDownX, touchDownY, touchUpX, touchUpY;
    public Texture imgPasted;
    public String typedText = "";

    @Override
    public void create() {
        selectedMode = Mode.DRAW;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        Gdx.input.setInputProcessor(new InputProcessorAOPi(this));

        font = getFont("fonts/Funnel.ttf");
        float screenHeight = Gdx.graphics.getHeight();
        float screenWidth = Gdx.graphics.getWidth();
        System.out.println(screenWidth + "x" + screenHeight);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        float screenHeight = Gdx.graphics.getHeight();
        float screenWidth = Gdx.graphics.getWidth();
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();

        batch.begin();
        int x = 10;
        int i = 0;
        for (Mode mode : Mode.values()) {
            i++;
            if (mode.equals(this.selectedMode)) {
                font.setColor(1, 1, 1, 1);
            } else {
                font.setColor(0.5f, 0.5f, 0.5f, 1);
            }
            font.draw(batch, mode.name() + " [" + i + "]", x, 10 + font.getXHeight());
            layout.setText(font, mode.name() + " [" + i + "]");
            x += layout.width + 10;
        }
        if (selectedMode.equals(Mode.TEXT)) {
            font.setColor(1, 1, 1, 1);
            font.draw(batch, typedText, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY() + font.getXHeight());
        }
        batch.end();

        batch.begin();
            for (Image image : images) {
                batch.draw(image.getTexture(), image.getX(), image.getY());
            }
            if (imgPasted != null) {
                batch.draw(imgPasted, mouseX, mouseY);
            }
        batch.end();

        batch.begin();
        //font.draw(batch, "MODE: " + selectedMode.g, 10, 10 + (font.getXHeight()*3));
        batch.end();

        if (Mode.DRAW.equals(selectedMode) && drawing) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 1, 1, 1); // rot
            touchUpX = mouseX - touchDownX;
            touchUpY = mouseY - touchDownY;
            shapeRenderer.rect(touchDownX, touchDownY, touchUpX, touchUpY);
            shapeRenderer.end();

            batch.begin();
            // let me see where we are
            font.draw(batch, mouseX + " x " + mouseY, mouseX, mouseY);
            batch.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Rectangle rectangle : rectangles) {
            shapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
        shapeRenderer.end();

    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    @NotNull
    private BitmapFont getFont(String fontPath) {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal(fontPath));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 24;
        parameter.color = Color.WHITE;
        BitmapFont f = generator.generateFont(parameter);
        generator.dispose();
        return f;
    }
}
