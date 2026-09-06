package io.github.HollowKnight.Model.World;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SafeSpawn {
    private final Rectangle bounds;

    public SafeSpawn(Rectangle bounds) {
        this.bounds = bounds;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
