package io.github.HollowKnight;

import io.github.HollowKnight.Model.Save.SaveManager;
import io.github.HollowKnight.Model.Settings.AudioManager;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.Achievement.AchievementManager;
import io.github.HollowKnight.View.BrightnessOverlay;
import io.github.HollowKnight.View.MainMenuScreen;

//package io.github.HollowKnight;

import com.badlogic.gdx.Game;


public class Main extends Game {
    private AssetLoader assetLoader;
    private GameSettings gameSettings;
    private KeyController keyController;
    private AudioManager audioManager;
    private BrightnessOverlay brightnessOverlay;
    private SaveManager saveManager;
    private AchievementManager achievementManager;

    @Override
    public void create() {
        assetLoader = new AssetLoader();
        assetLoader.loadAll();

        gameSettings = new GameSettings();
        gameSettings.load();

        keyController = new KeyController();
        keyController.loadFromPreferences(gameSettings.getPreferences());

        audioManager = new AudioManager(gameSettings);
        brightnessOverlay = new BrightnessOverlay(gameSettings, assetLoader.getSkin());
        saveManager = new SaveManager();
        achievementManager = new AchievementManager();

        this.setScreen(new MainMenuScreen(assetLoader.getSkin(), this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    public com.badlogic.gdx.scenes.scene2d.ui.Skin getSkin() {
        return assetLoader.getSkin();
    }

    public KeyController getKeyController() {
        return keyController;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public BrightnessOverlay getBrightnessOverlay() {
        return brightnessOverlay;
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (audioManager != null) {
            audioManager.dispose();
        }
        if (brightnessOverlay != null) {
            brightnessOverlay.dispose();
        }
        if (assetLoader != null){
            assetLoader.dispose();
        }
    }
}

