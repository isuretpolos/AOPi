package isuret.polos.aopi.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import isuret.polos.aopi.Main;
import isuret.polos.aopi.data.Database;
import isuret.polos.aopi.entities.Image;
import isuret.polos.aopi.entities.Text;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;

public class InputProcessorAOPi implements InputProcessor {
    private Main main;

    public InputProcessorAOPi(Main main) {
        this.main = main;
    }

    @Override
    public boolean keyDown(int keycode) {
        System.out.println("Key pressed: " + keycode);

        if (keycode == Input.Keys.ESCAPE) {
            Gdx.app.exit();
        }

        if (keycode == Input.Keys.F11) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1280, 800);
            } else {
                Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
                Gdx.graphics.setFullscreenMode(mode);
            }
        }

        if (keycode == Input.Keys.BACKSPACE) {
            if (!main.typedText.isEmpty()) {
                main.typedText = main.typedText.substring(0, main.typedText.length() - 1);
            }
            return true;
        }

        if (keycode == Input.Keys.V &&
            (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {

            try {
                Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

                if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    BufferedImage img = (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);

                    Pixmap pixmap = new Pixmap(img.getWidth(), img.getHeight(), Pixmap.Format.RGBA8888);

                    for (int y = 0; y < img.getHeight(); y++) {
                        for (int x = 0; x < img.getWidth(); x++) {
                            int argb = img.getRGB(x, y);

                            int a = (argb >> 24) & 0xff;
                            int r = (argb >> 16) & 0xff;
                            int g = (argb >> 8) & 0xff;
                            int b = argb & 0xff;

                            int rgba = (r << 24) | (g << 16) | (b << 8) | a;

                            pixmap.drawPixel(x, y, rgba);
                        }
                    }
                    main.imgPasted = new Texture(pixmap);
                    pixmap.dispose();
                }
            } catch (Exception _) {}
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode >= 145 && keycode <= 153) {
            try {
                main.selectedMode = Main.Mode.values()[keycode - 145];
                main.typedText = "";
                return true;
            } catch (Exception _) {}
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        if (main.selectedMode.equals(Main.Mode.TEXT) && Character.isLetterOrDigit(character)) {
            main.typedText += character;
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (Main.Mode.DRAW.equals(main.selectedMode) && button == Input.Buttons.LEFT) {
            main.drawing = true;
            float[] mousePos = main.getMousePosition();
            main.touchDownX = mousePos[0];
            main.touchDownY = mousePos[1];
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (Main.Mode.DRAW.equals(main.selectedMode) &&button == Input.Buttons.LEFT) {
            main.drawing = false;
            main.rectangles.add(new Rectangle(main.touchDownX, main.touchDownY, main.touchUpX, main.touchUpY));
        }
        if (Main.Mode.TEXT.equals(main.selectedMode) && button == Input.Buttons.LEFT) {
            float[] mousePos = main.getMousePosition();
            main.texts.add(new Text(main.typedText, mousePos[0], mousePos[1] + 12, 12, 1));
            Database.setValue("text-test",main.texts.get(main.texts.size()-1));
            main.typedText = "";
        }
        if (main.imgPasted != null) {
            main.images.add(new Image(main.imgPasted, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));
            main.imgPasted = null;
        }
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
