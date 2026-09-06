package io.github.HollowKnight.Model.Achievement;

public enum AchievementType {
    COMPLETION(
        "completion",
        "Path of Completion",
        "Finish the game.",
        "HollowKnight/Achievements/achievement_fast_completionist.png"
    ),
    SPEEDRUN(
        "speedrun",
        "Speedrunner",
        "Finish the game within the time limit.",
        "HollowKnight/Achievements/achievement_fast_finish.png"
    ),
    TRUE_HUNTER(
        "true_hunter",
        "True Hunter",
        "Defeat every enemy type at least once.",
        "HollowKnight/Achievements/achievement_Hunter_Marks.png"
    ),
    DEFEAT_FALSE_KNIGHT(
        "defeat_false_knight",
        "False Knight",
        "Defeat the False Knight.",
        "HollowKnight/Achievements/achievement_false_knight.png"
    ),
    EXPLORER(
        "explorer",
        "Explorer",
        "Enter every room in the kingdom.",
        "HollowKnight/Achievements/achievement_Hunter_Journal.png"
    );

    private final String id;
    private final String title;
    private final String description;
    private final String iconPath;

    AchievementType(String id, String title, String description, String iconPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconPath = iconPath;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIconPath() {
        return iconPath;
    }
}
