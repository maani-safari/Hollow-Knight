package io.github.HollowKnight.View;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.HollowKnight.Main;
import io.github.HollowKnight.Model.Save.GameData;
import io.github.HollowKnight.Model.Save.SaveManager;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileScreen extends BaseMenu{
    private Animation<TextureRegion> dividerAnimation;
    private Image dividerImage;
    private Texture[] dividerTextures;

    private Animation<TextureRegion> slotAnimation;
    private Texture[] slotTextures;

    private float menuStateTime = 0;
    public ProfileScreen(Skin skin, Main main) {
        super(skin, main);
    }

    @Override
    protected void showUI() {
        setBackGround("BackGround/controller_prompt_bg.png");
        GameSettings.Language lang = main.getGameSettings().getLanguage();
        dividerTextures = new Texture[9];
        TextureRegion[] dividerFrames = new TextureRegion[9];
        for (int i = 0; i < 9 ; i++){
            String frameNumber = String.format(Locale.US,"%04d", i);
            dividerTextures[i] = new Texture("ProfileScreen/Warning_fleur" + frameNumber + ".png");
            dividerFrames[i] = new TextureRegion(dividerTextures[i]);
        }
        dividerAnimation = new Animation<>(0.04f, dividerFrames);
        dividerAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        slotTextures = new Texture[13];
        TextureRegion[] slotFrames = new TextureRegion[13];
        for (int i = 0 ; i < 13 ; i++){
            String frameNumber = String.format(Locale.US,"%04d", i);
            slotTextures[i] = new Texture("ProfileScreen/profile_fleur" + frameNumber +".png");
            slotFrames[i] = new TextureRegion(slotTextures[i]);
        }
        slotAnimation = new Animation<>(0.04f, slotFrames);
        slotAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        Label titleLabel = new Label(SettingsLocalization.get("profile.title", lang), skin);
        table.add(titleLabel).padBottom(10).row();
        dividerImage = new Image(dividerFrames[0]) {
            @Override
            public void act(float delta) {
                super.act(delta);
                menuStateTime += delta;

                TextureRegion currentFrame = dividerAnimation.getKeyFrame(menuStateTime);
                setDrawable(new TextureRegionDrawable(currentFrame));
            }
        };
        table.add(dividerImage).width(600).height(35).padBottom(40).row();

        for (int i = 1; i <= 4; i++) {
            final int slotIndex = i;

            Button slotBtn = new Button() {
                @Override
                public void act(float delta) {
                    super.act(delta);
                    TextureRegion currentSlotFrame = slotAnimation.getKeyFrame(menuStateTime);
                    setStyle(new ButtonStyle(new TextureRegionDrawable(currentSlotFrame), null, null));
                }
            };

            Label numLabel = new Label(slotIndex + ".   ", skin);
            Label statusLabel = new Label(resolveSlotStatus(slotIndex, lang), skin);

            slotBtn.add(numLabel).padLeft(40);
            slotBtn.add(statusLabel).expandX().left();

            slotBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SaveManager saveManager = main.getSaveManager();
                    GameData data = saveManager.load(slotIndex);
                    main.setScreen(new GameScreen(main, data, slotIndex));
                }
            });

            table.add(slotBtn).width(750).height(90).padBottom(20).row();
        }

        TextButton backBtn = new TextButton(SettingsLocalization.get("back", lang), skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new MainMenuScreen(skin, main));
            }
        });
        table.add(backBtn).padTop(25).width(150);

        mainStack.add(table);
    }

    private String resolveSlotStatus(int slotIndex, GameSettings.Language lang) {
        SaveManager saveManager = main.getSaveManager();
        if (!saveManager.hasSave(slotIndex)) {
            return SettingsLocalization.get("profile.newGame", lang);
        }
        GameData data = saveManager.load(slotIndex);
        if (data == null) {
            return SettingsLocalization.get("profile.corrupted", lang);
        }
        String room = data.roomMapName != null ? data.roomMapName : "unknown";
        String time = "unknown time";
        if (data.saveTimestamp > 0L) {
            time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                .format(new Date(data.saveTimestamp));
        }
        return SettingsLocalization.format("profile.continue", lang, room, time);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (dividerTextures != null) {
            for (Texture tex : dividerTextures) {
                if (tex != null) tex.dispose();
            }
        }
        if (slotTextures != null) {
            for (Texture tex : slotTextures) {
                if (tex != null) tex.dispose();
            }
        }
    }
}
