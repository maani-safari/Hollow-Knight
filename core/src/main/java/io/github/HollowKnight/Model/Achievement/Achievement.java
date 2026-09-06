package io.github.HollowKnight.Model.Achievement;

import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

public class Achievement {
    private final AchievementType type;
    private boolean unlocked;
    private long unlockTimeMillis;

    public Achievement(AchievementType type) {
        this.type = type;
    }

    public AchievementType getType() {
        return type;
    }

    public String getId() {
        return type.getId();
    }

    public String getTitle() {
        return getTitle(GameSettings.Language.ENGLISH);
    }

    public String getTitle(GameSettings.Language language) {
        return SettingsLocalization.get("ach." + type.getId() + ".title", language);
    }

    public String getDescription() {
        return getDescription(GameSettings.Language.ENGLISH);
    }

    public String getDescription(GameSettings.Language language) {
        return SettingsLocalization.get("ach." + type.getId() + ".desc", language);
    }

    public String getIconPath() {
        return type.getIconPath();
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public long getUnlockTimeMillis() {
        return unlockTimeMillis;
    }

    public void unlock() {
        if (unlocked) {
            return;
        }
        unlocked = true;
        unlockTimeMillis = System.currentTimeMillis();
    }
}
