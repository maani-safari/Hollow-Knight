package io.github.HollowKnight;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AssetLoader {
    private Skin skin;
    private TextureAtlas atlas;
    private Cursor customCursor;

    public void loadAll() {
        skin = new Skin();
            atlas = new TextureAtlas(Gdx.files.internal("ui/HollowSkin.atlas"));
            skin.addRegions(atlas);

            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/TrajanPro-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
            param.size = 24;

            BitmapFont font = generator.generateFont(param);
            generator.dispose();

            skin.add("Hollowfont", font, BitmapFont.class);

            skin.load(Gdx.files.internal("ui/HollowSkin.json"));

            setupCursor();

    }

    private void setupCursor() {
        Pixmap originalPixmap = new Pixmap(Gdx.files.internal("ui/cursor.png"));

        Pixmap POTPixmap = new Pixmap(64, 64, originalPixmap.getFormat());
        POTPixmap.setBlending(Pixmap.Blending.None);

        POTPixmap.drawPixmap(originalPixmap, 0, 0);

        customCursor = Gdx.graphics.newCursor(POTPixmap, 0, 0);
        Gdx.graphics.setCursor(customCursor);
        originalPixmap.dispose();
        POTPixmap.dispose();

    }

    public Skin getSkin() {
        return skin;
    }

    public void dispose() {
        if (skin != null) skin.dispose();
        if (atlas != null) atlas.dispose();
        if (customCursor != null) customCursor.dispose();
    }
}
