package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.HollowKnight.Main;
import io.github.HollowKnight.Model.Achievement.Achievement;
import io.github.HollowKnight.Model.Achievement.AchievementManager;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

public class AchievementMenuView extends BaseMenu {

    private final AchievementManager achievementManager;
    private final Runnable onClose;
    private final ObjectMap<String, Texture> iconTextures = new ObjectMap<>();
    private Table listTable;

    public AchievementMenuView(Skin skin, Main main, AchievementManager achievementManager, Runnable onClose) {
        super(skin, main);
        this.achievementManager = achievementManager;
        this.onClose = onClose;
    }

    @Override
    public void show() {
        super.show();
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    onClose.run();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void showUI() {
        setBackGround("BackGround/Voidheart_menu_BG.png");

        Table root = new Table();
        root.setFillParent(true);
        root.pad(30);

        GameSettings.Language lang = main.getGameSettings().getLanguage();
        Label titleLabel = new Label(SettingsLocalization.get("achievements.title", lang), skin);
        root.add(titleLabel).padBottom(20).row();

        listTable = new Table();
        listTable.top().left();
        listTable.defaults().padBottom(10);

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        root.add(scrollPane).expand().fill().padBottom(20).row();

        TextButton backBtn = new TextButton(SettingsLocalization.get("back", lang), skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClose.run();
            }
        });
        root.add(backBtn).width(150);

        mainStack.add(root);
        refresh();
    }

    public void refresh() {
        if (listTable == null) {
            return;
        }
        listTable.clearChildren();
        for (Achievement achievement : achievementManager.getAllAchievements()) {
            listTable.add(buildAchievementRow(achievement)).width(760).row();
        }
    }

    private Table buildAchievementRow(Achievement achievement) {
        Table row = new Table();
        row.setBackground(skin.newDrawable("white-pixel", 0f, 0f, 0f, 0.35f));
        row.pad(12);

        Image icon = new Image(loadIcon(achievement.getIconPath()));
        if (achievement.isUnlocked()) {
            icon.setColor(Color.WHITE);
        } else {
            icon.setColor(0.45f, 0.45f, 0.45f, 0.75f);
        }

        GameSettings.Language lang = main.getGameSettings().getLanguage();
        Label titleLabel = new Label(achievement.getTitle(lang), skin);
        Label descriptionLabel = new Label(achievement.getDescription(lang), skin);
        descriptionLabel.setWrap(true);

        String statusText = achievement.isUnlocked()
            ? SettingsLocalization.get("achievements.unlocked", lang)
            : SettingsLocalization.get("achievements.locked", lang);
        Label statusLabel = new Label(statusText, skin);
        if (achievement.isUnlocked()) {
            statusLabel.setColor(0.7f, 0.9f, 0.6f, 1f);
        } else {
            statusLabel.setColor(0.65f, 0.65f, 0.65f, 1f);
        }

        Table content = new Table();
        content.add(titleLabel).left().row();
        content.add(descriptionLabel).width(560).left().padTop(4).row();
        content.add(statusLabel).left().padTop(6);

        row.add(icon).size(64).padRight(16).top();
        row.add(content).expand().fill().left();

        return row;
    }

    private TextureRegionDrawable loadIcon(String path) {
        Texture texture = iconTextures.get(path);
        if (texture == null) {
            if (Gdx.files.internal(path).exists()) {
                texture = new Texture(Gdx.files.internal(path));
            } else {
                texture = new Texture(Gdx.files.internal("ui/title.png"));
            }
            iconTextures.put(path, texture);
        }
        return new TextureRegionDrawable(texture);
    }

    @Override
    public void dispose() {
        for (Texture texture : iconTextures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        iconTextures.clear();
        super.dispose();
    }
}
