package io.github.HollowKnight.Model.Settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Central settings model for audio, display, and language preferences.
 * Persists values via LibGDX {@link Preferences}.
 */
public class GameSettings {

    public enum Language {
        ENGLISH("en"),
        FRENCH("fr");

        private final String code;

        Language(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public Language toggle() {
            return this == ENGLISH ? FRENCH : ENGLISH;
        }

        public static Language fromStoredName(String name) {
            if (name == null || name.isBlank()) {
                return ENGLISH;
            }
            if ("PERSIAN".equals(name)) {
                return FRENCH;
            }
            try {
                return Language.valueOf(name);
            } catch (IllegalArgumentException e) {
                return ENGLISH;
            }
        }
    }

    private static final String PREFS_NAME = "hollowknight_settings";

    private static final String KEY_MUSIC_VOLUME = "musicVolume";
    private static final String KEY_MUSIC_ENABLED = "musicEnabled";
    private static final String KEY_SFX_ENABLED = "sfxEnabled";
    private static final String KEY_BRIGHTNESS = "brightness";
    private static final String KEY_LANGUAGE = "language";

    private static final float DEFAULT_MUSIC_VOLUME = 0.7f;
    private static final boolean DEFAULT_MUSIC_ENABLED = true;
    private static final boolean DEFAULT_SFX_ENABLED = true;
    private static final float DEFAULT_BRIGHTNESS = 1.0f;
    private static final Language DEFAULT_LANGUAGE = Language.ENGLISH;

    private float musicVolume = DEFAULT_MUSIC_VOLUME;
    private boolean musicEnabled = DEFAULT_MUSIC_ENABLED;
    private boolean sfxEnabled = DEFAULT_SFX_ENABLED;
    private float brightness = DEFAULT_BRIGHTNESS;
    private Language language = DEFAULT_LANGUAGE;

    private final Preferences prefs;

    public GameSettings() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public void load() {
        musicVolume = prefs.getFloat(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
        musicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, DEFAULT_MUSIC_ENABLED);
        sfxEnabled = prefs.getBoolean(KEY_SFX_ENABLED, DEFAULT_SFX_ENABLED);
        brightness = prefs.getFloat(KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS);
        language = Language.fromStoredName(prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE.name()));
    }

    public void save() {
        prefs.putFloat(KEY_MUSIC_VOLUME, musicVolume);
        prefs.putBoolean(KEY_MUSIC_ENABLED, musicEnabled);
        prefs.putBoolean(KEY_SFX_ENABLED, sfxEnabled);
        prefs.putFloat(KEY_BRIGHTNESS, brightness);
        prefs.putString(KEY_LANGUAGE, language.name());
        prefs.flush();
    }

    public void resetAudio() {
        musicVolume = DEFAULT_MUSIC_VOLUME;
        musicEnabled = DEFAULT_MUSIC_ENABLED;
        sfxEnabled = DEFAULT_SFX_ENABLED;
        save();
    }

    public Preferences getPreferences() {
        return prefs;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = Math.clamp(musicVolume, 0f, 1f);
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public void setSfxEnabled(boolean sfxEnabled) {
        this.sfxEnabled = sfxEnabled;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = Math.clamp(brightness, 0f, 1f);
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
