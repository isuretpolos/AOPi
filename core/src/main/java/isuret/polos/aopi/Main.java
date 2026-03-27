package isuret.polos.aopi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import isuret.polos.aopi.data.Database;
import isuret.polos.aopi.entities.Image;
import isuret.polos.aopi.entities.Text;
import isuret.polos.aopi.input.InputProcessorAOPi;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    public Viewport viewport;
    public Vector3 mouse = new Vector3();

    public enum Mode {
        OPERATE, EDIT, DRAW, TEXT
    }

    public Mode selectedMode;
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
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        camera.update();
        mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        //camera.unproject(mouse);
        viewport.unproject(mouse);
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // DRAW batch always before shapeRenderer
        batch.begin();
        // TEST see the mouse position
        font.draw(batch, mouse.x + " x " + mouse.y, mouse.x, mouse.y);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (Mode.DRAW.equals(selectedMode) && drawing) {
            shapeRenderer.setColor(1, 0, 1, 1);
            shapeRenderer.rect(touchDownX, touchDownY, mouse.x - touchDownX, mouse.y - touchDownY);
        }

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

}
