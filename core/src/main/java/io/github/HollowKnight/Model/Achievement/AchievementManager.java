package io.github.HollowKnight.Model.Achievement;

import io.github.HollowKnight.Controller.GameEvent;
import io.github.HollowKnight.Controller.GameEventBus;
import io.github.HollowKnight.Controller.GameEventPayload;
import io.github.HollowKnight.Model.entity.enemy.EnemyType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AchievementManager {

    public static final float DEFAULT_SPEEDRUN_TIME_LIMIT_SECONDS = 300f;

    private static final List<String> ALL_ROOMS = List.of(
        "room2.tmx",
        "room-test.tmx"
    );

    private static final Set<EnemyType> HUNTER_TARGETS = EnumSet.allOf(EnemyType.class);

    private final Map<AchievementType, Achievement> achievements = new EnumMap<>(AchievementType.class);
    private final Set<EnemyType> defeatedEnemyTypes = EnumSet.noneOf(EnemyType.class);
    private final Set<String> visitedRooms = new java.util.HashSet<>();

    private float speedrunTimeLimitSeconds = DEFAULT_SPEEDRUN_TIME_LIMIT_SECONDS;
    private GameEventBus eventBus;

    public AchievementManager() {
        for (AchievementType type : AchievementType.values()) {
            achievements.put(type, new Achievement(type));
        }
    }

    public void bind(GameEventBus eventBus) {
        this.eventBus = eventBus;
        registerListeners(eventBus);
    }

    public List<Achievement> getAllAchievements() {
        return Arrays.stream(AchievementType.values())
            .map(achievements::get)
            .toList();
    }

    public Achievement getAchievement(AchievementType type) {
        return achievements.get(type);
    }

    public float getSpeedrunTimeLimitSeconds() {
        return speedrunTimeLimitSeconds;
    }

    public void setSpeedrunTimeLimitSeconds(float speedrunTimeLimitSeconds) {
        this.speedrunTimeLimitSeconds = speedrunTimeLimitSeconds;
    }

    private void registerListeners(GameEventBus bus) {
        bus.subscribe(GameEvent.FALSE_KNIGHT_DEFEATED, () -> tryUnlock(AchievementType.DEFEAT_FALSE_KNIGHT));

        bus.subscribe(GameEvent.ENEMY_KILLED, (GameEventPayload.EnemyKilled payload) -> {
            if (payload == null || payload.enemyType() == null) {
                return;
            }
            defeatedEnemyTypes.add(payload.enemyType());
            checkTrueHunter();
        });

        bus.subscribe(GameEvent.ROOM_ENTERED, (GameEventPayload.RoomEntered payload) -> {
            if (payload == null || payload.roomMapName() == null || payload.roomMapName().isEmpty()) {
                return;
            }
            visitedRooms.add(payload.roomMapName());
            checkExplorer();
        });

        bus.subscribe(GameEvent.GAME_FINISHED, (GameEventPayload.GameFinished payload) -> {
            tryUnlock(AchievementType.COMPLETION);
            if (payload != null && payload.totalPlayTimeSeconds() <= speedrunTimeLimitSeconds) {
                tryUnlock(AchievementType.SPEEDRUN);
            }
        });
    }

    private void checkTrueHunter() {
        if (defeatedEnemyTypes.containsAll(HUNTER_TARGETS)) {
            tryUnlock(AchievementType.TRUE_HUNTER);
        }
    }

    private void checkExplorer() {
        if (visitedRooms.containsAll(ALL_ROOMS)) {
            tryUnlock(AchievementType.EXPLORER);
        }
    }

    private void tryUnlock(AchievementType type) {
        Achievement achievement = achievements.get(type);
        if (achievement == null || achievement.isUnlocked()) {
            return;
        }
        achievement.unlock();
        if (eventBus != null) {
            eventBus.post(GameEvent.ACHIEVEMENT_UNLOCKED, achievement);
        }
    }
}
