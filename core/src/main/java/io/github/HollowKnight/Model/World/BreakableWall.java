package io.github.HollowKnight.Model.World;

import com.badlogic.gdx.math.Rectangle;

public class BreakableWall {
    public static final int HIT_TO_BREAK = 3;
    private final Rectangle hitbox;
    private int hitTake;
    private boolean broken;

    public BreakableWall(float x , float y , float width , float height) {
        this.hitbox = new Rectangle(x, y, width, height);
        this.hitTake = 0;
        this.broken = false;
    }
    public boolean hit(){
        if (broken) return false;
        hitTake++;
        if (hitTake >= HIT_TO_BREAK){
            broken = true;
            return true;
        }
        return false;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public boolean isBroken() {
        return broken;
    }

    public int getHitTake() {
        return hitTake;
    }
    public int getHitRemaining(){
        return  Math.max(0 , HIT_TO_BREAK - hitTake);
    }
}
