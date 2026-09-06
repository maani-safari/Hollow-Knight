package io.github.HollowKnight.View;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.HollowKnight.Controller.SettingsController;
import io.github.HollowKnight.Main;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.KeyController.BindingId;
import io.github.HollowKnight.Model.Settings.KeyController.RebindableBinding;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

/**
 * Scene2D settings menu. UI only — logic lives in {@link SettingsController}.
 */
public class SettingsView extends BaseMenu {

    private final SettingsController controller;
    private final ObjectMap<BindingId, TextButton> bindingButtons = new ObjectMap<>();
    private Label musicVolumeValueLabel;
    private Label brightnessValueLabel;
    private TextButton languageButton;
    private Table contentTable;

    public SettingsView(Skin skin, Main main) {
        super(skin, main);
        this.controller = new SettingsController(
            main,
            main.getGameSettings(),
            main.getAudioManager(),
            main.getKeyController()
        );
    }

    @Override
    public void show() {
        super.show();
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (controller.isRebinding()) {
                    if (keycode == Input.Keys.ESCAPE) {
                        controller.cancelRebind();
                        refreshBindingButtons();
                        return true;
                    }
                    controller.applyRebind(keycode);
                    refreshBindingButtons();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    controller.close();
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

        GameSettings.Language lang = controller.getSettings().getLanguage();
        Label titleLabel = new Label(SettingsLocalization.get("title", lang), skin);
        root.add(titleLabel).padBottom(20).row();

        contentTable = new Table();
        contentTable.top().left();
        contentTable.defaults().left().padBottom(6);

        buildAudioSection(lang);
        buildDisplaySection(lang);
        buildLanguageSection(lang);
        buildControlsSection(lang);

        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        root.add(scrollPane).expand().fill().padBottom(20).row();

        TextButton backBtn = new TextButton(SettingsLocalization.get("back", lang), skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.close();
            }
        });
        root.add(backBtn).width(150);

        mainStack.add(root);
    }

    private void buildAudioSection(GameSettings.Language lang) {
        addSectionHeader(SettingsLocalization.get("audio", lang));

        Table volumeRow = new Table();
        volumeRow.add(new Label(SettingsLocalization.get("musicVolume", lang), skin)).width(220).left();
        musicVolumeValueLabel = new Label(formatPercent(controller.getSettings().getMusicVolume()), skin);
        volumeRow.add(musicVolumeValueLabel).width(60).left().padRight(10);

        Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(controller.getSettings().getMusicVolume());
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float value = volumeSlider.getValue();
                controller.setMusicVolume(value);
                musicVolumeValueLabel.setText(formatPercent(value));
            }
        });
        volumeRow.add(volumeSlider).width(280).left();
        contentTable.add(volumeRow).padBottom(8).row();

        CheckBox musicCheck = new CheckBox(" " + SettingsLocalization.get("musicEnabled", lang), skin);
        musicCheck.setChecked(controller.getSettings().isMusicEnabled());
        musicCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                controller.setMusicEnabled(musicCheck.isChecked());
            }
        });
        contentTable.add(musicCheck).padBottom(6).row();

        CheckBox sfxCheck = new CheckBox(" " + SettingsLocalization.get("sfxEnabled", lang), skin);
        sfxCheck.setChecked(controller.getSettings().isSfxEnabled());
        sfxCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                controller.setSfxEnabled(sfxCheck.isChecked());
            }
        });
        contentTable.add(sfxCheck).padBottom(10).row();

        TextButton resetAudioBtn = new TextButton(SettingsLocalization.get("resetAudio", lang), skin);
        resetAudioBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.resetAudio();
                rebuildContent();
            }
        });
        contentTable.add(resetAudioBtn).width(180).padBottom(16).row();
    }

    private void buildDisplaySection(GameSettings.Language lang) {
        addSectionHeader(SettingsLocalization.get("display", lang));

        Table brightnessRow = new Table();
        brightnessRow.add(new Label(SettingsLocalization.get("brightness", lang), skin)).width(220).left();
        brightnessValueLabel = new Label(formatPercent(controller.getSettings().getBrightness()), skin);
        brightnessRow.add(brightnessValueLabel).width(60).left().padRight(10);

        Slider brightnessSlider = new Slider(0f, 1f, 0.01f, false, skin);
        brightnessSlider.setValue(controller.getSettings().getBrightness());
        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float value = brightnessSlider.getValue();
                controller.setBrightness(value);
                brightnessValueLabel.setText(formatPercent(value));
            }
        });
        brightnessRow.add(brightnessSlider).width(280).left();
        contentTable.add(brightnessRow).padBottom(16).row();
    }

    private void buildLanguageSection(GameSettings.Language lang) {
        addSectionHeader(SettingsLocalization.get("language", lang));

        String langLabel = lang == GameSettings.Language.ENGLISH
            ? SettingsLocalization.get("langEnglish", lang)
            : SettingsLocalization.get("langFrench", lang);

        languageButton = new TextButton(langLabel, skin);
        languageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.toggleLanguage();
                rebuildContent();
            }
        });
        contentTable.add(languageButton).width(180).padBottom(16).row();
    }

    private void buildControlsSection(GameSettings.Language lang) {
        addSectionHeader(SettingsLocalization.get("controls", lang));

        bindingButtons.clear();
        for (RebindableBinding binding : controller.getRebindableBindings()) {
            Table row = new Table();
            row.add(new Label(SettingsLocalization.get("bind." + binding.id().name(), lang), skin)).width(220).left();

            TextButton keyBtn = new TextButton(controller.getBindingDisplay(binding.id()), skin);
            keyBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    controller.startRebind(binding.id());
                    refreshBindingButtons();
                }
            });
            bindingButtons.put(binding.id(), keyBtn);
            row.add(keyBtn).width(160).left();
            contentTable.add(row).padBottom(4).row();
        }

        TextButton resetControlsBtn = new TextButton(SettingsLocalization.get("resetControls", lang), skin);
        resetControlsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.resetControls();
                refreshBindingButtons();
            }
        });
        contentTable.add(resetControlsBtn).width(180).padTop(8).row();
    }

    private void refreshBindingButtons() {
        GameSettings.Language lang = controller.getSettings().getLanguage();
        String waitingText = SettingsLocalization.get("pressKey", lang);
        for (ObjectMap.Entry<BindingId, TextButton> entry : bindingButtons) {
            if (controller.isRebinding() && controller.getRebindingTarget() == entry.key) {
                entry.value.setText(waitingText);
            } else {
                entry.value.setText(controller.getBindingDisplay(entry.key));
            }
        }
    }

    private void rebuildContent() {
        mainStack.clearChildren();
        showUI();
    }

    private void addSectionHeader(String text) {
        Label header = new Label(text, skin);
        contentTable.add(header).padTop(12).padBottom(10).row();
    }

    private static String formatPercent(float value) {
        return Math.round(value * 100f) + "%";
    }
}
