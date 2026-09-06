package io.github.HollowKnight.Controller;

import io.github.HollowKnight.Model.entity.enemy.EnemyType;

public final class GameEventPayload {

    private GameEventPayload() {
    }

    public record EnemyKilled(EnemyType enemyType) {
    }

    public record RoomEntered(String roomMapName) {
    }

    public record GameFinished(float totalPlayTimeSeconds) {
    }
}
