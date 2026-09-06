package io.github.HollowKnight.View;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.HollowKnight.Model.Settings.GameSettings;


public class BrightnessOverlay {

    private final GameSettings settings;
    private final TextureRegion whitePixel;
    private final SpriteBatch batch;

    public BrightnessOverlay(GameSettings settings, Skin skin) {
        this.settings = settings;
        this.whitePixel = skin.getRegion("white-pixel");
        this.batch = new SpriteBatch();
    }

    public void draw(float screenWidth, float screenHeight) {
        float darkness = 1f - settings.getBrightness();
        if (darkness < 0.001f) {
            return;
        }
        batch.begin();
        batch.setColor(0f, 0f, 0f, darkness);
        batch.draw(whitePixel, 0f, 0f, screenWidth, screenHeight);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    public void dispose() {
        batch.dispose();
    }
}
