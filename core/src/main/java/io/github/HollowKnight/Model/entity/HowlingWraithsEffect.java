package io.github.HollowKnight.Model.entity;

import com.badlogic.gdx.math.Rectangle;

public class HowlingWraithsEffect {

    public static final float TOTAL_DURATION = 0.5f;
    public static final int   HIT_COUNT      = 3;
    public static final float HIT_INTERVAL   = TOTAL_DURATION / HIT_COUNT;
    public static final float WIDTH           = 100f;
    public static final float HEIGHT           = 70f;

    private final Rectangle hitbox;
    private final int       damagePerHit;
    private float           timer;
    private int             hitsApplied;
    private boolean         expired;

    public HowlingWraithsEffect(float knightX, float knightY, float knightWidth, int damagePerHit) {
        float x = knightX + knightWidth / 2f - WIDTH / 2f;
        float y = knightY - 50f;
        this.hitbox        = new Rectangle(x, y, WIDTH, HEIGHT);
        this.damagePerHit  = damagePerHit;
        this.timer          = 0f;
        this.hitsApplied    = 0;
        this.expired         = false;
    }

    public void update(float delta) {
        timer += delta;
        if (timer >= TOTAL_DURATION) expired = true;
    }
    public boolean consumePendingHit() {
        int expectedHits = Math.min(HIT_COUNT, (int) (timer / HIT_INTERVAL) + 1);
        if (expectedHits > hitsApplied) {
            hitsApplied = expectedHits;
            return true;
        }
        return false;
    }

    public Rectangle getHitbox()   { return hitbox; }
    public int getDamagePerHit()   { return damagePerHit; }
    public boolean isExpired()     { return expired; }

    public float getTimer() {
        return timer;
    }
}
