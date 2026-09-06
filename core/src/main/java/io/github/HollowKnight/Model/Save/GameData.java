package io.github.HollowKnight.Model.Save;


public class GameData {

    public float knightX;
    public float knightY;

    public float lastSafeX;
    public float lastSafeY;

    public int healthMasks;
    public int maxHealthMasks;

    public int currentSouls;

    public String[] equippedCharms;
    public String[] unlockedCharms;

    public String roomMapName;

    public boolean falseKnightDefeated;

    public String[] completedNpcDialogues;

    public SettingsSnapshot settings;

    public long saveTimestamp;

    public float totalPlayTimeSeconds;

    public int totalDeaths;

    public int totalEnemiesKilled;


    public static class SettingsSnapshot {
        public float musicVolume;
        public boolean musicEnabled;
        public boolean sfxEnabled;
        public float brightness;
        public String language;

        public int keyMoveLeft;
        public int keyMoveRight;
        public int keyJump;
        public int keyDash;
        public int keyAttack;
        public int keyFocus;
        public int keySpell;
        public int keyInventory;
        public int keyInteract;
        public int keyPause;
    }
}
