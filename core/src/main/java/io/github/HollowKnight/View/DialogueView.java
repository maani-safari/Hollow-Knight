package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class DialogueView {

  private static final float PANEL_HEIGHT_RATIO = 0.28f;
  private static final float BLINK_INTERVAL = 0.5f;

  private final Stage stage;
  private final Label speakerLabel;
  private final Label textLabel;
  private final Label continueLabel;
  private boolean open;
  private float blinkTimer;
  private boolean blinkVisible = true;

  public DialogueView(Skin skin) {
    stage = new Stage(new ScreenViewport());

    Table root = new Table();
    root.setFillParent(true);
    root.bottom().padBottom(24f);

    Table panel = new Table(skin);
    panel.setBackground(skin.newDrawable("white-pixel", 0.05f, 0.05f, 0.08f, 0.82f));
    panel.pad(20f);

    speakerLabel = new Label("", skin);
    textLabel = new Label("", skin);
    textLabel.setWrap(true);
    continueLabel = new Label("▼", skin);

    panel.add(speakerLabel).left().padBottom(8f).row();
    panel.add(textLabel).width(Gdx.graphics.getWidth() * 0.75f).left().padBottom(10f).row();
    panel.add(continueLabel).right();

    root.add(panel).width(Gdx.graphics.getWidth() * 0.85f).height(Gdx.graphics.getHeight() * PANEL_HEIGHT_RATIO);
    stage.addActor(root);
  }

  public boolean isOpen() {
    return open;
  }

  public void open(String speakerName, String line) {
    open = true;
    speakerLabel.setText(speakerName);
    textLabel.setText(line);
    blinkTimer = 0f;
    blinkVisible = true;
    continueLabel.setVisible(true);
  }

  public void setLine(String line) {
    textLabel.setText(line);
    blinkTimer = 0f;
    blinkVisible = true;
    continueLabel.setVisible(true);
  }

  public void close() {
    open = false;
    speakerLabel.setText("");
    textLabel.setText("");
    continueLabel.setVisible(false);
  }

  public void update(float delta) {
    if (!open) {
      return;
    }
    blinkTimer += delta;
    if (blinkTimer >= BLINK_INTERVAL) {
      blinkTimer = 0f;
      blinkVisible = !blinkVisible;
    }
    continueLabel.setVisible(blinkVisible);
    stage.act(delta);
  }

  public void render(float delta) {
    if (!open) {
      return;
    }
    update(delta);
    stage.draw();
  }

  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  public void dispose() {
    stage.dispose();
  }
}
