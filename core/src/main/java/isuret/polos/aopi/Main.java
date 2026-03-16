package isuret.polos.aopi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout layout = new GlyphLayout();
    private boolean drawing = false;
    private List<Rectangle> rectangles = new ArrayList<>();
    private float touchDownX, touchDownY, touchUpX, touchUpY;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        shapeRenderer = new ShapeRenderer();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                System.out.println("Key pressed: " + keycode);
                if (keycode == Input.Keys.NUM_1) {
                    drawing = !drawing;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                }
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    drawing = true;
                    touchDownX = screenX;
                    touchDownY = Gdx.graphics.getHeight() - screenY;
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    drawing = false;
                    rectangles.add(new Rectangle(touchDownX, touchDownY, touchUpX, touchUpY));
                }
                return true;
            }
        });

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/Funnel.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 24;      // Schriftgröße
        parameter.color = Color.WHITE;

        font = generator.generateFont(parameter);

        generator.dispose();

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
        font.draw(batch, "[0]HIDE TOOLS [1]DRAW", 10, 10 + font.getXHeight());
        batch.end();

        if (drawing) {
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



        /*batch.begin();
        //batch.draw(image, 140, 210);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1); // rot
        shapeRenderer.circle(300, 300, 50);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 0, 1); // rot
        shapeRenderer.line(100, 100, 300, 200);
        shapeRenderer.rect(50, 50, 200, 120);
        shapeRenderer.end();

        batch.end();*/
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
