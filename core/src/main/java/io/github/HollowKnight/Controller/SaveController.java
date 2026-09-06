package io.github.HollowKnight.Controller;

import com.badlogic.gdx.math.Vector2;
import io.github.HollowKnight.Main;
import io.github.HollowKnight.Model.Save.GameData;
import io.github.HollowKnight.Model.Save.SaveManager;
import io.github.HollowKnight.Model.Settings.AudioManager;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.charm.Charm;
import io.github.HollowKnight.Model.charm.CharmInventory;
import io.github.HollowKnight.Model.charm.Charms;
import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.npc.NpcDialogueState;
import io.github.HollowKnight.Model.npc.NpcDialogueState;

import java.util.ArrayList;
import java.util.List;


public class SaveController {

    private final Main main;
    private final SaveManager saveManager;

    public SaveController(Main main, SaveManager saveManager) {
        this.main = main;
        this.saveManager = saveManager;
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }


    public GameData captureGameState(
        Knight knight,
        CharmInventory charmInventory,
        String roomMapName,
        boolean falseKnightDefeated,
        float totalPlayTimeSeconds,
        int totalDeaths,
        int totalEnemiesKilled,
        NpcDialogueState npcDialogueState
    ) {
        GameData data = new GameData();

        Vector2 pos = knight.getPosition();
        data.knightX = pos.x;
        data.knightY = pos.y;
        data.lastSafeX = knight.getLastSafeX();
        data.lastSafeY = knight.getLastSafeY();

        data.healthMasks = knight.getHealthMasks();
        data.maxHealthMasks = knight.getMaxHealthMasks();
        data.currentSouls = knight.getSoulVessel().getCurrentSouls();

        data.equippedCharms = charmIdsFromCharms(knight.getCharmSlots().getEquipped());
        data.unlockedCharms = charmIdsFromCharms(charmInventory.getOwned());

        data.roomMapName = roomMapName;
        data.falseKnightDefeated = falseKnightDefeated;
        data.totalPlayTimeSeconds = totalPlayTimeSeconds;
        data.totalDeaths = totalDeaths;
        data.totalEnemiesKilled = totalEnemiesKilled;
        data.completedNpcDialogues = npcDialogueState != null
            ? npcDialogueState.toSaveArray()
            : new String[0];
        data.settings = captureSettingsSnapshot();

        return data;
    }


    public boolean saveGame(
        int slot,
        Knight knight,
        CharmInventory charmInventory,
        String roomMapName,
        boolean falseKnightDefeated,
        float totalPlayTimeSeconds,
        int totalDeaths,
        int totalEnemiesKilled,
        NpcDialogueState npcDialogueState
    ) {
        GameData data = captureGameState(
            knight,
            charmInventory,
            roomMapName,
            falseKnightDefeated,
            totalPlayTimeSeconds,
            totalDeaths,
            totalEnemiesKilled,
            npcDialogueState
        );
        return saveManager.save(slot, data);
    }


    public GameData loadGame(int slot) {
        return saveManager.load(slot);
    }


    public void applyGameData(GameData data, Knight knight, CharmInventory charmInventory) {
        applyGameData(data, knight, charmInventory, null);
    }

    public void applyGameData(
        GameData data,
        Knight knight,
        CharmInventory charmInventory,
        NpcDialogueState npcDialogueState
    ) {
        if (data == null) {
            return;
        }

        knight.restoreFromSave(
            data.knightX,
            data.knightY,
            data.lastSafeX,
            data.lastSafeY,
            data.healthMasks,
            data.maxHealthMasks,
            data.currentSouls
        );

        charmInventory.restoreOwned(charmsFromIds(data.unlockedCharms));
        knight.getCharmSlots().clear();
        for (Charms charm : charmsFromIds(data.equippedCharms)) {
            if (charmInventory.owns(charm)) {
                knight.equipCharm(charm);
            }
        }

        applySettingsSnapshot(data.settings);

        if (npcDialogueState != null) {
            npcDialogueState.restoreFromSave(data.completedNpcDialogues);
        }
    }

    private GameData.SettingsSnapshot captureSettingsSnapshot() {
        GameSettings settings = main.getGameSettings();
        KeyController keys = main.getKeyController();

        GameData.SettingsSnapshot snapshot = new GameData.SettingsSnapshot();
        snapshot.musicVolume = settings.getMusicVolume();
        snapshot.musicEnabled = settings.isMusicEnabled();
        snapshot.sfxEnabled = settings.isSfxEnabled();
        snapshot.brightness = settings.getBrightness();
        snapshot.language = settings.getLanguage().name();

        snapshot.keyMoveLeft = keys.getMoveLeft();
        snapshot.keyMoveRight = keys.getMoveRight();
        snapshot.keyJump = keys.getJump();
        snapshot.keyDash = keys.getDash();
        snapshot.keyAttack = keys.getAttack();
        snapshot.keyFocus = keys.getFocus();
        snapshot.keySpell = keys.getVengeful();
        snapshot.keyInventory = keys.getInventory();
        snapshot.keyInteract = keys.getInteract();
        snapshot.keyPause = keys.getPause();

        return snapshot;
    }

    private void applySettingsSnapshot(GameData.SettingsSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        GameSettings settings = main.getGameSettings();
        AudioManager audio = main.getAudioManager();
        KeyController keys = main.getKeyController();

        settings.setMusicVolume(snapshot.musicVolume);
        settings.setMusicEnabled(snapshot.musicEnabled);
        settings.setSfxEnabled(snapshot.sfxEnabled);
        settings.setBrightness(snapshot.brightness);
        settings.setLanguage(GameSettings.Language.fromStoredName(snapshot.language));
        settings.save();

        audio.setMusicVolume(settings.getMusicVolume());
        audio.setMusicEnabled(settings.isMusicEnabled());
        audio.setSfxEnabled(settings.isSfxEnabled());

        keys.setMoveLeft(snapshot.keyMoveLeft);
        keys.setMoveRight(snapshot.keyMoveRight);
        keys.setJump(snapshot.keyJump);
        keys.setDash(snapshot.keyDash);
        keys.setAttack(snapshot.keyAttack);
        keys.setFocus(snapshot.keyFocus);
        keys.setVengeful(snapshot.keySpell);
        keys.setInventory(snapshot.keyInventory);
        if (snapshot.keyInteract != 0) {
            keys.setInteract(snapshot.keyInteract);
        }
        keys.setPause(snapshot.keyPause);
        keys.saveToPreferences(settings.getPreferences());
    }

    private static String[] charmIdsFromCharms(Iterable<? extends Charm> charms) {
        List<String> ids = new ArrayList<>();
        for (Charm charm : charms) {
            ids.add(charm.getId());
        }
        return ids.toArray(new String[0]);
    }

    private static List<Charms> charmsFromIds(String[] ids) {
        List<Charms> charms = new ArrayList<>();
        if (ids == null) {
            return charms;
        }
        for (String id : ids) {
            Charms charm = Charms.fromId(id);
            if (charm != null) {
                charms.add(charm);
            }
        }
        return charms;
    }
}
