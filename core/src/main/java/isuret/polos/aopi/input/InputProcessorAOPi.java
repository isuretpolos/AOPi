package isuret.polos.aopi.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import isuret.polos.aopi.Main;
import isuret.polos.aopi.devices.Device;
import isuret.polos.aopi.entities.IEntity;
import isuret.polos.aopi.entities.Image;
import isuret.polos.aopi.entities.Layer;
import isuret.polos.aopi.entities.ShapeTool;

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
        } else if (keycode == Input.Keys.F1) {
            main.selectedMode = Main.Mode.F1_Operation;
        } else if (keycode == Input.Keys.F2) {
            main.selectedMode = Main.Mode.F2_DeviceEditor;
        } else if (keycode == Input.Keys.F3) {
            main.selectedMode = Main.Mode.F3_Help;
        } else if (keycode == Input.Keys.F4) {
            main.selectedMode = Main.Mode.F4_Case;
        } else if (keycode == Input.Keys.F5) {
            main.selectedMode = Main.Mode.F5_Analyze;
        } else if (keycode == Input.Keys.F6) {
            main.selectedMode = Main.Mode.F6_Broadcast;
        } else if (keycode == Input.Keys.F7) {
            main.selectedMode = Main.Mode.F7_Database;
        } else if (keycode == Input.Keys.F8) {
            main.selectedMode = Main.Mode.F8_RatesTrends;
        } else if (keycode == Input.Keys.F9) {
            main.selectedMode = Main.Mode.F9_Reports;
        } else if (keycode == Input.Keys.F10) {
            main.selectedMode = Main.Mode.F10_Settings;
        } else if (keycode == Input.Keys.F11) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1280, 800);
            } else {
                Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
                Gdx.graphics.setFullscreenMode(mode);
            }
        } else if (keycode == Input.Keys.F12) {
            main.selectedMode = Main.Mode.F12_Resonance;
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
        /*if (main.selectedMode.equals(Main.Mode.TEXT) && Character.isLetterOrDigit(character)) {
            main.typedText += character;
        }*/
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (Main.Mode.F2_DeviceEditor.equals(main.selectedMode) && Main.SubModeEditor.N_NewDevice.equals(main.selectedSubMode) && button == Input.Buttons.LEFT) {
            main.drawing = true;
            main.touchDownX = main.mouse.x;
            main.touchDownY = main.mouse.y;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector3 tmp = new Vector3(screenX, screenY, 0);
        main.viewport.unproject(tmp);
        if (Main.Mode.F2_DeviceEditor.equals(main.selectedMode) && Main.SubModeEditor.N_NewDevice.equals(main.selectedSubMode) && button == Input.Buttons.LEFT) {
            main.drawing = false;
            main.selectedSubMode = Main.SubModeEditor.G_MoveDevice;
            Rectangle rect = ShapeTool.createNormalizedRectangle(main.touchDownX, main.touchDownY, tmp.x, tmp.y);
            Device dev = new Device("Test", "Test", "Test", "Test", "Test", rect);
            main.getLayer(Main.LayerType.Device).getEntities().add(dev);
        }
        /*if (Main.Mode.TEXT.equals(main.selectedMode) && button == Input.Buttons.LEFT) {
            main.texts.add(new Text(main.typedText, Gdx.input.getX(), Gdx.input.getY() + 12, 12, 1));
            Database.setValue("text-test",main.texts.get(main.texts.size()-1));
            main.typedText = "";
        }*/
        if (main.imgPasted != null) {
            main.images.add(new Image(main.imgPasted, tmp.x, tmp.y));
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

        Vector3 tmp = new Vector3(screenX, screenY, 0);
        main.viewport.unproject(tmp);

        for (Layer layer : main.layers) {
            for (IEntity entity : layer.getEntities()) {
                entity.setMouseOver(false);
            }
        }

        IEntity hit = findEntityAt(tmp.x, tmp.y);
        if (hit != null) {
            hit.setMouseOver(true);
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    private IEntity findEntityAt(float worldX, float worldY) {
        // reverse order so that the topmost entity is checked first
        for (int j=main.layers.size() - 1; j >= 0; j--) {
            Layer layer = main.layers.get(j);
            for (int i = layer.getEntities().size() - 1; i >= 0; i--) {
                IEntity entity = layer.getEntities().get(i);
                if (entity.getBounds().contains(worldX, worldY)) {
                    return entity;
                }
            }
        }
        return null;
    }
}
