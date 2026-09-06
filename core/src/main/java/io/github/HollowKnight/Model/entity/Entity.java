package io.github.HollowKnight.Model.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Entity {
    protected Vector2 position;
    protected Vector2 velocity;
    protected Rectangle hitbox;
    protected int maxHp;
    protected int currentHp;
    protected boolean facingRight;
    protected boolean alive;

    public Entity(float spawnX, float spawnY, float width , float height , int maxHp ) {
        this.position = new Vector2(spawnX , spawnY);
        this.velocity = new Vector2(0 , 0 );
        this.hitbox = new Rectangle(spawnX , spawnY , width , height);
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.facingRight = true;
        this.alive = true;
    }
    public void syncHitbox(){
        hitbox.setPosition(position);
    }
    public void takeDamage(int damage){
        currentHp = Math.max(0 , currentHp - damage);
        if (currentHp == 0){
            alive = false;
        }
    }

    public Vector2 getVelocity() {
        return velocity;
    }
    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }
    public Vector2 getPosition() {
        return position;
    }
    public void setPosition(Vector2 position) {
        this.position.set(position);
        syncHitbox();
    }
    public boolean isFacingRight() {
        return facingRight;
    }
    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }
    public boolean isAlive() {
        return alive;
    }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    public Rectangle getHitbox() {
        return hitbox;
    }
    public int getMaxHp() {
        return maxHp;
    }
    public int getCurrentHp() {
        return currentHp;
    }

}
