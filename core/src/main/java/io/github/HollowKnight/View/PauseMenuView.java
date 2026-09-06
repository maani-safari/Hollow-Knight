package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.HollowKnight.Model.Cheat.CheatCatalog;
import io.github.HollowKnight.Model.Cheat.CheatCatalog.CheatEntry;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;


public class PauseMenuView {

    public interface Listener {
        void onResume();
        void onSaveGame();
        void onQuitToMainMenu();
        void onSetting();
    }

    private final Stage stage;
    private final Label statusLabel;
    private Listener listener;
    private boolean open;

    public PauseMenuView(Skin skin, KeyController keys, GameSettings.Language language) {
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("white-pixel", 0f, 0f, 0f, 0.55f));
        Table cheatTable = new Table();
        Table menuTable = new Table();
        Label titleLabel = new Label(SettingsLocalization.get("pause.title", language), skin);
        statusLabel = new Label("", skin);

        TextButton resumeBtn = new TextButton(SettingsLocalization.get("pause.resume", language), skin);
        TextButton saveBtn = new TextButton(SettingsLocalization.get("pause.save", language), skin);
        TextButton settingBtn = new TextButton(SettingsLocalization.get("pause.options", language), skin);
        TextButton menuBtn = new TextButton(SettingsLocalization.get("pause.mainMenu", language), skin);

        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    listener.onResume();
                }
            }
        });

        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    listener.onSaveGame();
                }
            }
        });

        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    listener.onQuitToMainMenu();
                }
            }
        });
        settingBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null)
                    listener.onSetting();
            }
        });
        cheatTable.add(new Label(SettingsLocalization.get("pause.cheatsHeader", language), skin)).left().padBottom(10).row();
        for (CheatEntry cheat : CheatCatalog.getEntries(keys, language)) {
            cheatTable.add(new Label(cheat.name() + " : " + cheat.shortcut(), skin)).left().row();
        }

        menuTable.add(titleLabel).padBottom(20).row();
        menuTable.add(resumeBtn).width(220).padBottom(12).row();
        menuTable.add(saveBtn).width(220).padBottom(12).row();
        menuTable.add(settingBtn).width(220).padBottom(12).row();
        menuTable.add(menuBtn).width(220).padBottom(20).row();
        menuTable.add(statusLabel).padBottom(10).row();
        menuTable.add(new Label(SettingsLocalization.get("pause.quickHint", language), skin));

        root.add(cheatTable).expandX().left().padRight(50);
        root.add(menuTable).expand().center();
        stage.addActor(root);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        Gdx.input.setInputProcessor(open ? stage : null);
        if (!open) {
            clearStatus();
        }
    }

    public void toggle() {
        setOpen(!open);
    }

    public void showStatus(String message) {
        statusLabel.setText(message);
    }

    public void clearStatus() {
        statusLabel.setText("");
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void render(float delta) {
        if (!open) {
            return;
        }
        stage.act(delta);
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
    }
}
