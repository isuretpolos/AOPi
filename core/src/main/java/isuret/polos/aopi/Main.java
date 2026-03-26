package isuret.polos.aopi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import isuret.polos.aopi.data.Database;
import isuret.polos.aopi.entities.Image;
import isuret.polos.aopi.entities.Text;
import isuret.polos.aopi.input.InputProcessorAOPi;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 mouse = new Vector3();

    public enum Mode {
        OPERATE, EDIT, DRAW, TEXT
    }

    public Mode selectedMode;
    public Mode selectedSubMode;
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;
    public GlyphLayout layout = new GlyphLayout();
    public boolean drawing = false;
    public List<Text> texts = new ArrayList<>();
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

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.update();

        Gdx.input.setInputProcessor(new InputProcessorAOPi(this));

        font = getFont("fonts/Funnel.ttf");
        float screenHeight = Gdx.graphics.getHeight();
        float screenWidth = Gdx.graphics.getWidth();
        System.out.println(screenWidth + "x" + screenHeight);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        float[] mousePos = getMousePosition();
        float mouseX = mousePos[0];
        float mouseY = mousePos[1];

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
        for (Text text : texts) {
            font.setColor(1, 1, 1, 1);
            font.draw(batch, text.getText(), text.getX(), text.getY());
        }
        if (selectedMode.equals(Mode.TEXT)) {
            font.setColor(1, 1, 1, 1);
            font.draw(batch, typedText, mouseX, mouseY + font.getXHeight());
        }

        for (Image image : images) {
            batch.draw(image.getTexture(), image.getX(), image.getY());
        }
        if (imgPasted != null) {
            batch.draw(imgPasted, mouseX, mouseY);
        }

        for (Rectangle rectangle : rectangles) {
            font.draw(batch, rectangle.toString(), rectangle.x, rectangle.y + 20 + rectangle.height);
        }
        font.draw(batch, mouseX + " x " + mouseY, mouseX, mouseY + font.getXHeight());
        //font.draw(batch, "MODE: " + selectedMode.g, 10, 10 + (font.getXHeight()*3));
        batch.end();

        if (Mode.DRAW.equals(selectedMode) && drawing) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 1, 1, 1);
            touchUpX = mousePos[0] - touchDownX;
            touchUpY = mousePos[1] - touchDownY;
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
        Database.save();
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public float[] getMousePosition() {
        mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mouse);
        return new float[]{mouse.x, mouse.y};
    }
}
