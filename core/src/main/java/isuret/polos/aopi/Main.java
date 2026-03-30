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
import isuret.polos.aopi.devices.Button;
import isuret.polos.aopi.devices.Device;
import isuret.polos.aopi.devices.IntentionRepeater;
import isuret.polos.aopi.entities.IEntity;
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
        F1Operation,
        F2DeviceEditor,
        F3Help,
        F4Case,
        F5Analyze,
        F6Broadcast,
        F7Database,
        F8RatesTrends,
        F9Reports,
        F10Settings,
        F11Fullscreen,
        F12Debug
    }

    public Mode selectedMode;
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;
    public GlyphLayout layout = new GlyphLayout();
    public boolean drawing = false;
    public List<Text> texts = new ArrayList<>();
    public List<Image> images = new ArrayList<>();
    public List<IEntity> entities = new ArrayList<>();
    public float touchDownX, touchDownY;
    public Texture imgPasted;
    public String typedText = "";

    @Override
    public void create() {
        selectedMode = Mode.F2DeviceEditor;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.update();

        Gdx.input.setInputProcessor(new InputProcessorAOPi(this));

        font = getFont("fonts/Funnel.ttf");

        float width = 300;
        float y = Gdx.graphics.getHeight() - 10;

        addButton("F12 Debug", 10, y, width, 30); y -= 35;
        addButton("F11 Fullscreen", 10, y, width, 30); y -= 35;
        addButton("F10 Settings", 10, y, width, 30); y -= 35;
        addButton("F9 Reports", 10, y, width, 30); y -= 35;
        addButton("F8 Rates / Trends", 10, y, width, 30); y -= 35;
        addButton("F7 Database", 10, y, width, 30); y -= 35;
        addButton("F6 Broadcast", 10, y, width, 30); y -= 35;
        addButton("F5 Analyze", 10, y, width, 30); y -= 35;
        addButton("F4 Case", 10, y, width, 30); y -= 35;
        addButton("F3 Help", 10, y, width, 30); y -= 35;
        addButton("F2 Device Editor", 10, y, width, 30); y -= 35;
        addButton("F1 Operation", 10, y, width, 30); y -= 35;

        IntentionRepeater intentionRepeater = new IntentionRepeater();
        Vector3 tmp = new Vector3(10, y, 0);
        viewport.unproject(tmp);
        intentionRepeater.setBounds(new Rectangle(tmp.x, tmp.y, width, 30));
        intentionRepeater.startBroadcast();
        entities.add(intentionRepeater);

    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        camera.update();
        mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mouse);
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        if (Mode.F2DeviceEditor.equals(selectedMode)) {

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            for (int i = 0; i < Gdx.graphics.getWidth(); i += 40) {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.2f);
                shapeRenderer.line(i, 0, i, Gdx.graphics.getHeight());
            }
            for (int i = 0; i < Gdx.graphics.getHeight(); i += 40) {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.2f);
                shapeRenderer.line(0, i, Gdx.graphics.getWidth(), i);
            }

            if (drawing) {
                shapeRenderer.setColor(1, 1, 1, 1);
                shapeRenderer.rect(touchDownX, touchDownY, mouse.x - touchDownX, mouse.y - touchDownY);
            }
            shapeRenderer.end();

            for (IEntity entity : entities) {
                entity.draw(shapeRenderer, mouse);
            }

            batch.begin();
            font.draw(batch, mouse.x + " x " + mouse.y, mouse.x, mouse.y);
            batch.end();
        }

        batch.begin();
        for (IEntity entity : entities) {
            entity.render(batch, font, mouse);
        }
        batch.end();

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

    private void addButton(String text, float x, float y, float width, float height) {
        Button button = new Button();
        button.setText(text);
        Vector3 tmp = new Vector3(x, y, 0);
        viewport.unproject(tmp);
        button.setBounds(new Rectangle(tmp.x,tmp.y,width,height));
        entities.add(button);
    }

}
