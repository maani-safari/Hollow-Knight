package io.github.HollowKnight.Controller;

import io.github.HollowKnight.Main;
import io.github.HollowKnight.Model.Settings.AudioManager;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.Settings.KeyController.BindingId;
import io.github.HollowKnight.Model.Settings.KeyController.RebindableBinding;
import io.github.HollowKnight.View.MainMenuScreen;

import java.util.List;


public class SettingsController {

    private final Main main;
    private final GameSettings settings;
    private final AudioManager audio;
    private final KeyController keys;

    private BindingId rebindingTarget;

    public SettingsController(Main main, GameSettings settings, AudioManager audio, KeyController keys) {
        this.main = main;
        this.settings = settings;
        this.audio = audio;
        this.keys = keys;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public List<RebindableBinding> getRebindableBindings() {
        return keys.getRebindableBindings();
    }

    public String getBindingDisplay(BindingId id) {
        return KeyController.formatKey(keys.getBindingKey(id));
    }

    public boolean isRebinding() {
        return rebindingTarget != null;
    }

    public BindingId getRebindingTarget() {
        return rebindingTarget;
    }

    public void startRebind(BindingId target) {
        rebindingTarget = target;
    }

    public void cancelRebind() {
        rebindingTarget = null;
    }

    public boolean applyRebind(int keyCode) {
        if (rebindingTarget == null) {
            return false;
        }
        keys.setBindingKey(rebindingTarget, keyCode);
        keys.saveToPreferences(settings.getPreferences());
        rebindingTarget = null;
        return true;
    }

    public void setMusicVolume(float volume) {
        audio.setMusicVolume(volume);
    }

    public void setMusicEnabled(boolean enabled) {
        audio.setMusicEnabled(enabled);
    }

    public void setSfxEnabled(boolean enabled) {
        audio.setSfxEnabled(enabled);
    }

    public void resetAudio() {
        audio.resetAudio();
    }

    public void setBrightness(float brightness) {
        settings.setBrightness(brightness);
        settings.save();
    }

    public void toggleLanguage() {
        settings.setLanguage(settings.getLanguage().toggle());
        settings.save();
    }

    public void resetControls() {
        keys.resetGameplayBindings();
        keys.saveToPreferences(settings.getPreferences());
    }

    public void close() {
        main.setScreen(new MainMenuScreen(main.getSkin(), main));
    }
}
