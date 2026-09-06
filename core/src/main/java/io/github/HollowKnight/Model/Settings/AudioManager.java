package io.github.HollowKnight.Model.Settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import io.github.HollowKnight.Controller.GameEvent;
import io.github.HollowKnight.Controller.GameEventBus;


public class AudioManager {

    private static final String MUSIC_PATH = "audio/title.wav";
    private static final String NAIL_SLASH_PATH = "audio/hero_dream_nail_slash_only.wav";
    private static final String ENEMY_DAMAGE_PATH = "audio/enemy_damage.wav";
    private static final String PLAYER_DAMAGE_PATH = "audio/hero_damage.wav";
    private static final String SOUL_GAIN_PATH = "audio/soul_pickup_1.wav";
    private static final String FOCUS_COMPLETE_PATH = "audio/focus_health_heal.wav";
    private static final String[] ZOTE_VOICE_PATHS = {
        "audio/zote/Zote_01.wav",
        "audio/zote/Zote_02.wav",
        "audio/zote/Zote_03.wav",
        "audio/zote/Zote_04.wav",
        "audio/zote/Zote_05.wav"
    };
    private static final String ZOTE_HURT_PATH = "audio/zote/Zote_battle_death.wav";

    private final GameSettings settings;
    private final RandomXS128 random = new RandomXS128();
    private Music backgroundMusic;
    private final Array<Sound> zoteVoiceSounds = new Array<>();
    private Sound zoteHurtSound;
    private Sound nailSlashSound;
    private Sound enemyDamageSound;
    private Sound playerDamageSound;
    private Sound soulGainSound;
    private Sound focusCompleteSound;
    private int lastVoiceIndex = -1;
    private String currentPath;

    public AudioManager(GameSettings settings) {
        this.settings = settings;
        loadMusic();
        loadZoteSounds();
        loadGameplaySounds();
        applyMusicState();
    }

    private void loadMusic() {

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_PATH));
        backgroundMusic.setLooping(true);
    }

    private void loadZoteSounds() {
        for (String path : ZOTE_VOICE_PATHS) {
            if (!Gdx.files.internal(path).exists()) {
                continue;
            }
            zoteVoiceSounds.add(Gdx.audio.newSound(Gdx.files.internal(path)));
        }
        if (Gdx.files.internal(ZOTE_HURT_PATH).exists()) {
            zoteHurtSound = Gdx.audio.newSound(Gdx.files.internal(ZOTE_HURT_PATH));
        }
    }

    private void loadGameplaySounds() {
        nailSlashSound = loadSound(NAIL_SLASH_PATH);
        enemyDamageSound = loadSound(ENEMY_DAMAGE_PATH);
        playerDamageSound = loadSound(PLAYER_DAMAGE_PATH);
        soulGainSound = loadSound(SOUL_GAIN_PATH);
        focusCompleteSound = loadSound(FOCUS_COMPLETE_PATH);
    }

    private Sound loadSound(String path) {
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("AudioManager", "Missing sound: " + path);
            return null;
        }
        return Gdx.audio.newSound(Gdx.files.internal(path));
    }

    private void playSfx(Sound sound, float volume) {
        if (!settings.isSfxEnabled() || sound == null) {
            return;
        }
        sound.play(Math.clamp(volume, 0f, 1f));
    }

    public void playNailSlash() {
        playSfx(nailSlashSound, 0.65f);
    }

    public void playEnemyDamage() {
        playSfx(enemyDamageSound, 0.8f);
    }

    public void playPlayerDamage() {
        playSfx(playerDamageSound, 1.0f);
    }

    public void playSoulGain() {
        playSfx(soulGainSound, 0.7f);
    }

    public void playFocusComplete() {
        playSfx(focusCompleteSound, 0.9f);
    }

    public void bindGameplayEvents(GameEventBus eventBus) {
        eventBus.subscribe(GameEvent.KNIGHT_ATTACKED, this::playNailSlash);
        eventBus.subscribe(GameEvent.KNIGHT_HIT_ENEMY, this::playEnemyDamage);
        eventBus.subscribe(GameEvent.KNIGHT_TOOK_DAMAGE, this::playPlayerDamage);
        eventBus.subscribe(GameEvent.KNIGHT_GAINED_SOUL, this::playSoulGain);
        eventBus.subscribe(GameEvent.KNIGHT_HEALED, this::playFocusComplete);
    }

    public void playMusic(String path){
        if (path == null || path.equals(currentPath) ) return;
        if (backgroundMusic!=null){
            backgroundMusic.stop();
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
        if (!Gdx.files.internal(path).exists())return;
        currentPath = path;
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        backgroundMusic.setLooping(true);
        if (settings.isMusicEnabled()){
            backgroundMusic.setVolume(settings.getMusicVolume());
            backgroundMusic.play();
        }

    }
    public void playRandomZoteVoice() {
        if (!settings.isSfxEnabled() || zoteVoiceSounds.isEmpty()) {
            return;
        }

        int index = random.nextInt(zoteVoiceSounds.size);
        if (zoteVoiceSounds.size > 1) {
            while (index == lastVoiceIndex) {
                index = random.nextInt(zoteVoiceSounds.size);
            }
        }
        lastVoiceIndex = index;
        zoteVoiceSounds.get(index).play(settings.getMusicVolume());
    }

    public void playZoteHurt() {
        if (!settings.isSfxEnabled() || zoteHurtSound == null) {
            return;
        }
        zoteHurtSound.play(settings.getMusicVolume());
    }

    public void applyMusicState() {
        if (backgroundMusic == null) {
            return;
        }
        if (settings.isMusicEnabled()) {
            backgroundMusic.setVolume(settings.getMusicVolume());
            if (!backgroundMusic.isPlaying()) {
                backgroundMusic.play();
            }
        } else {
            backgroundMusic.pause();
        }
    }

    public void setMusicVolume(float volume) {
        settings.setMusicVolume(volume);
        if (backgroundMusic != null && settings.isMusicEnabled()) {
            backgroundMusic.setVolume(volume);
        }
        settings.save();
    }

    public void setMusicEnabled(boolean enabled) {
        settings.setMusicEnabled(enabled);
        if (backgroundMusic == null) {
            settings.save();
            return;
        }
        if (enabled) {
            backgroundMusic.setVolume(settings.getMusicVolume());
            backgroundMusic.play();
        } else {
            backgroundMusic.pause();
        }
        settings.save();
    }

    public void setSfxEnabled(boolean enabled) {
        settings.setSfxEnabled(enabled);
        settings.save();
    }

    public void resetAudio() {
        settings.resetAudio();
        applyMusicState();
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(settings.getMusicVolume());
        }
    }

    public boolean isSfxEnabled() {
        return settings.isSfxEnabled();
    }

    public float getMusicVolume() {
        return settings.getMusicVolume();
    }

    public boolean isMusicEnabled() {
        return settings.isMusicEnabled();
    }

    public void dispose() {
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
        for (Sound sound : zoteVoiceSounds) {
            sound.dispose();
        }
        zoteVoiceSounds.clear();
        if (zoteHurtSound != null) {
            zoteHurtSound.dispose();
            zoteHurtSound = null;
        }
        disposeSound(nailSlashSound);
        nailSlashSound = null;
        disposeSound(enemyDamageSound);
        enemyDamageSound = null;
        disposeSound(playerDamageSound);
        playerDamageSound = null;
        disposeSound(soulGainSound);
        soulGainSound = null;
        disposeSound(focusCompleteSound);
        focusCompleteSound = null;
    }

    private void disposeSound(Sound sound) {
        if (sound != null) {
            sound.dispose();
        }
    }
    public void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
        currentPath = null;
    }
}
