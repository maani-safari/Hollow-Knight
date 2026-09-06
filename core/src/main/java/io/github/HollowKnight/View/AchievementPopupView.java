package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.HollowKnight.Model.Achievement.Achievement;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

public class AchievementPopupView {

    private static final float DISPLAY_SECONDS = 2f;
    private static final float ANIMATION_SECONDS = 0.35f;

    private final Stage stage;
    private final Skin skin;
    private final GameSettings.Language language;
    private final Table popup;
    private final Label titleLabel;
    private final Label nameLabel;
    private final Image iconImage;
    private final ObjectMap<String, Texture> iconTextures = new ObjectMap<>();
    private final Queue<Achievement> queue = new Queue<>();

    private boolean animating;

    public AchievementPopupView(Skin skin, GameSettings.Language language) {
        this.skin = skin;
        this.language = language;
        this.stage = new Stage(new ScreenViewport());

        popup = new Table();
        popup.setBackground(skin.newDrawable("white-pixel", 0.05f, 0.05f, 0.08f, 0.92f));
        popup.pad(16);
        popup.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);

        titleLabel = new Label(SettingsLocalization.get("achievements.popup", language), skin);
        nameLabel = new Label("", skin);
        iconImage = new Image();

        popup.add(titleLabel).padBottom(8).row();
        popup.add(iconImage).size(48).padBottom(8).row();
        popup.add(nameLabel);

        stage.addActor(popup);
        popup.setVisible(false);
    }

    public void enqueue(Achievement achievement) {
        if (achievement == null) {
            return;
        }
        queue.addLast(achievement);
        if (!animating) {
            showNext();
        }
    }

    private void showNext() {
        if (queue.isEmpty()) {
            animating = false;
            popup.setVisible(false);
            return;
        }

        Achievement achievement = queue.removeFirst();
        animating = true;
        popup.setVisible(true);
        popup.getColor().a = 0f;

        nameLabel.setText(achievement.getTitle(language));
        iconImage.setDrawable(loadIcon(achievement.getIconPath()));
        iconImage.setColor(Color.WHITE);

        popup.pack();
        stage.act(0f);
        float targetY = stage.getViewport().getWorldHeight() - popup.getPrefHeight() - 24f;
        float startY = stage.getViewport().getWorldHeight() + 20f;
        popup.setPosition(
            (stage.getViewport().getWorldWidth() - popup.getPrefWidth()) / 2f,
            startY
        );

        popup.clearActions();
        popup.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(ANIMATION_SECONDS),
                Actions.moveTo(popup.getX(), targetY, ANIMATION_SECONDS)
            ),
            Actions.delay(DISPLAY_SECONDS),
            Actions.fadeOut(ANIMATION_SECONDS),
            Actions.run(this::showNext)
        ));
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

    public void render(float delta) {
        if (!animating && queue.isEmpty()) {
            return;
        }
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        for (Texture texture : iconTextures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        iconTextures.clear();
        stage.dispose();
    }
}
