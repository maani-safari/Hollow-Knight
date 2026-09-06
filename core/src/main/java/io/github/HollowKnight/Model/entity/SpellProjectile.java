package io.github.HollowKnight.Model.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SpellProjectile {
    public static final float SPEED = 280f;
    public static final float MAX_LIFETIME = 1.8f;
    public static final float WIDTH = 150f;
    public static final float HEIGHT = 50f;

    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle hitbox;
    private final int damage;
    private float lifeTimer;
    private boolean expired;

    public SpellProjectile(float startX , float startY, boolean facingRight , int damage) {
        this.position = new Vector2(startX , startY);
        this.velocity = new Vector2(facingRight ? SPEED : -SPEED,0);
        this.hitbox = new Rectangle(startX, startY, WIDTH, HEIGHT);
        this.damage = damage;
        this.lifeTimer = 0;
        this.expired = false;
    }
    public void  update(float delta){
        position.mulAdd(velocity , delta);
        hitbox.setPosition(position);
        lifeTimer += delta;
        if (lifeTimer >= MAX_LIFETIME) expired = true;
    }
    public Rectangle getHitbox() { return hitbox; }
    public int getDamage()       { return damage; }
    public boolean isExpired()   { return expired; }
    public void expire()         { expired = true; }

    public float getLifeTimer() {
        return lifeTimer;
    }
    public boolean isFacingRight() { return velocity.x > 0; }
}
