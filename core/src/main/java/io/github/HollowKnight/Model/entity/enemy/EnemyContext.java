package io.github.HollowKnight.Model.entity.enemy;

import com.badlogic.gdx.math.Vector2;
import io.github.HollowKnight.Model.World.Room;

public class EnemyContext {
    public final Vector2 knightPosition;
    public final boolean knightAlive;
    public final Room room;

    public EnemyContext(Vector2 knightPosition, boolean knightAlive, Room room) {
        this.knightPosition = knightPosition;
        this.knightAlive    = knightAlive;
        this.room           = room;
    }

    public float distanceToKnight(Vector2 myPosition) {
        return myPosition.dst(knightPosition);
    }

    public boolean knightIsLeftOf(Vector2 myPosition) {
        return knightPosition.x < myPosition.x;
    }
}
