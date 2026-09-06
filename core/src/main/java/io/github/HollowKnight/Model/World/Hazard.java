package io.github.HollowKnight.Model.World;

import com.badlogic.gdx.math.Rectangle;

public class Hazard {
    private final Rectangle hitbox;
    private final int damage;

    public Hazard(float x , float y , float width , float height) {
        this.hitbox = new Rectangle(x, y, width, height);
        this.damage = 1;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public int getDamage() {
        return damage;
    }
}
