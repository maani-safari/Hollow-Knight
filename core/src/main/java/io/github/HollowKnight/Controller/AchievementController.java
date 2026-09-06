package io.github.HollowKnight.Controller;

import io.github.HollowKnight.Model.Achievement.Achievement;
import io.github.HollowKnight.Model.Achievement.AchievementManager;
import io.github.HollowKnight.View.AchievementMenuView;
import io.github.HollowKnight.View.AchievementPopupView;

public class AchievementController {

    private final AchievementManager achievementManager;
    private final GameEventBus eventBus;
    private final AchievementPopupView popupView;
    private AchievementMenuView menuView;

    public AchievementController(
        AchievementManager achievementManager,
        GameEventBus eventBus,
        AchievementPopupView popupView
    ) {
        this.achievementManager = achievementManager;
        this.eventBus = eventBus;
        this.popupView = popupView;
    }

    public void bind() {
        achievementManager.bind(eventBus);
        eventBus.subscribe(GameEvent.ACHIEVEMENT_UNLOCKED, (Achievement achievement) -> {
            if (popupView != null) {
                popupView.enqueue(achievement);
            }
            if (menuView != null) {
                menuView.refresh();
            }
        });
    }

    public void setMenuView(AchievementMenuView menuView) {
        this.menuView = menuView;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }
}
