package io.github.HollowKnight.Model.Save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;


public class SaveManager {

    public static final int MIN_SLOT = 1;
    public static final int MAX_SLOT = 4;
    public static final int DEFAULT_SLOT = 1;

    private static final String SAVE_DIRECTORY = "saves/";
    private static final String SAVE_FILE_PREFIX = "save";
    private static final String SAVE_FILE_SUFFIX = ".json";

    private final Json json;

    public SaveManager() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
    }

    public FileHandle getSaveFile(int slot) {
        validateSlot(slot);
        FileHandle directory = Gdx.files.local(SAVE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory.child(SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX);
    }


    public boolean save(int slot, GameData data) {
        if (data == null) {
            return false;
        }
        try {
            data.saveTimestamp = System.currentTimeMillis();
            FileHandle file = getSaveFile(slot);
            file.writeString(json.prettyPrint(data), false);
            Gdx.app.log("SaveManager", "Saved slot " + slot + " to " + file.path());
            return true;
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to save slot " + slot, e);
            return false;
        }
    }


    public GameData load(int slot) {
        validateSlot(slot);
        FileHandle file = getSaveFile(slot);
        if (!file.exists()) {
            return null;
        }
        try {
            GameData data = json.fromJson(GameData.class, file);
            if (!isValid(data)) {
                Gdx.app.error("SaveManager", "Save slot " + slot + " failed validation.");
                return null;
            }
            return data;
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to load save slot " + slot, e);
            return null;
        }
    }

    public boolean hasSave(int slot) {
        validateSlot(slot);
        return getSaveFile(slot).exists();
    }

    public boolean deleteSave(int slot) {
        validateSlot(slot);
        FileHandle file = getSaveFile(slot);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    public GameData loadMostRecent() {
        GameData mostRecent = null;
        int mostRecentSlot = -1;
        for (int slot = MIN_SLOT; slot <= MAX_SLOT; slot++) {
            GameData data = load(slot);
            if (data == null) {
                continue;
            }
            if (mostRecent == null || data.saveTimestamp > mostRecent.saveTimestamp) {
                mostRecent = data;
                mostRecentSlot = slot;
            }
        }
        if (mostRecent != null) {
            Gdx.app.log("SaveManager", "Most recent save found in slot " + mostRecentSlot);
        }
        return mostRecent;
    }

    public int findMostRecentSlot() {
        GameData mostRecent = null;
        int mostRecentSlot = DEFAULT_SLOT;
        for (int slot = MIN_SLOT; slot <= MAX_SLOT; slot++) {
            GameData data = load(slot);
            if (data == null) {
                continue;
            }
            if (mostRecent == null || data.saveTimestamp > mostRecent.saveTimestamp) {
                mostRecent = data;
                mostRecentSlot = slot;
            }
        }
        return mostRecentSlot;
    }

    private boolean isValid(GameData data) {
        if (data == null) {
            return false;
        }
        if (data.roomMapName == null || data.roomMapName.isEmpty()) {
            return false;
        }
        if (data.maxHealthMasks <= 0) {
            return false;
        }
        return data.healthMasks >= 0 && data.healthMasks <= data.maxHealthMasks;
    }

    private void validateSlot(int slot) {
        if (slot < MIN_SLOT || slot > MAX_SLOT) {
            throw new IllegalArgumentException("Save slot must be between " + MIN_SLOT + " and " + MAX_SLOT);
        }
    }
}
