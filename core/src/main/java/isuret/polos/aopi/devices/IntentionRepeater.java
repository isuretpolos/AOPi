package isuret.polos.aopi.devices;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import isuret.polos.aopi.services.IntentionBroadcastService;

public class IntentionRepeater extends Device {

    private final IntentionBroadcastService repeaterService = new IntentionBroadcastService();
    private String statusText = "";
    private float progressBar = 0f;

    public void startBroadcast() {
        repeaterService.broadcastRepetition(
            "I am calm and focused.",
            "00:00:10",
            0.25,
            "EXACT",
            0,
            false,
            0,
            1_000_000L,
            0,
            0,
            false,
            false,
            "HZ",
            null,
            null,
            new IntentionBroadcastService.ProgressListener() {
                @Override
                public void onPrepared(IntentionBroadcastService.BroadcastProgress p) {
                    statusText = "Prepared: " + p.preparedIntentionLength + " chars";
                    progressBar = 0f;
                }

                @Override
                public void onProgress(IntentionBroadcastService.BroadcastProgress p) {
                    statusText = "[" + p.runtimeFormatted + "] "
                        + p.totalIterationsDisplay + " / "
                        + p.totalFrequencyDisplay + "Hz";
                    progressBar = p.completionRatio;
                }

                @Override
                public void onCompleted(IntentionBroadcastService.BroadcastProgress p) {
                    statusText = p.stopped ? "Stopped" : "Completed";
                    progressBar = 1f;
                }

                @Override
                public void onError(Throwable error) {
                    statusText = "Error: " + error.getMessage();
                }
            }
        );
    }

    public void stopBroadcast() {
        repeaterService.stopBroadcast();
    }

    @Override
    public void render(SpriteBatch batch, BitmapFont font, Vector3 mouse) {
        font.draw(batch, statusText + progressBar, bounds.x + 10, bounds.y + (font.getLineHeight()/1.33f));
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, Vector3 mouse) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // background (red)
        shapeRenderer.setColor(1f, 0f, 0f, 1f);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        // progress (green)
        shapeRenderer.setColor(0f, 1f, 0f, 1f);
        shapeRenderer.rect(
            bounds.x + 2,
            bounds.y + 2,
            bounds.width * progressBar,
            bounds.height - 4
        );

        shapeRenderer.end();
    }
}
