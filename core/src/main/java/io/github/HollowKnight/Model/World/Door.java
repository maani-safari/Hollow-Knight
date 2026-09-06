package io.github.HollowKnight.Model.World;

import com.badlogic.gdx.math.Rectangle;

public class Door {
    private final Rectangle hitbox;
    private final String targetRoom;
    private final String targetSpawn;

    public Door(Rectangle hitbox, String targetRoom, String targetSpawn) {
        this.hitbox = new Rectangle(hitbox);
        this.targetRoom = targetRoom;
        this.targetSpawn = targetSpawn;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public String getTargetRoom() {
        return targetRoom;
    }

    public String getTargetSpawn() {
        return targetSpawn;
    }
}
