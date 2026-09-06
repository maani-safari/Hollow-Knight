package io.github.HollowKnight.View;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

public class InteractionHintView {

  private final Stage stage;
  private final Label hintLabel;
  private boolean visible;

  public InteractionHintView(Skin skin, GameSettings.Language language) {
    stage = new Stage(new ScreenViewport());

    Table root = new Table();
    root.setFillParent(true);
    root.center();

    hintLabel = new Label(SettingsLocalization.get("hint.talk", language), skin);
    hintLabel.setVisible(false);
    root.add(hintLabel);

    stage.addActor(root);
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
    hintLabel.setVisible(visible);
  }

  public boolean isVisible() {
    return visible;
  }

  public void render(float delta) {
    if (!visible) {
      return;
    }
    stage.act(delta);
    stage.draw();
  }

  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  public void dispose() {
    stage.dispose();
  }
}
