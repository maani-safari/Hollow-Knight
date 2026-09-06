package io.github.HollowKnight.Model.entity.enemy;

import io.github.HollowKnight.Model.entity.Entity;

public class Enemy extends Entity {
    protected EnemyState enemyState;
    protected float stateTimer;
    protected int soulReward;
    protected int contactDamage;
    protected float hurtStunned;
    protected String type;
    protected boolean readyToBeRemoved = false;
    private final static float DEATH_DURATION = 1.2f;

    public Enemy(float spawnX, float spawnY, float width, float height, int maxHp, int soulReward, int contactDamage) {
        super(spawnX, spawnY, width, height, maxHp);
        this.enemyState = EnemyState.IDLE;
        this.stateTimer = 0f;
        this.soulReward = soulReward;
        this.contactDamage = contactDamage;
        this.hurtStunned = 0.2f;
    }

    public void update(float delta, EnemyContext ctx) {

    }

    public void onHit(int amount) {
        if (enemyState == EnemyState.DEATH) return;
        takeDamage(amount);
        if (!isAlive()) {
            enemyState = EnemyState.DEATH;
            stateTimer = 0;
            readyToBeRemoved = false;
        } else {
            enemyState = EnemyState.HURT;
            stateTimer = 0;
        }
    }

    public void tickState(float delta) {
        stateTimer += delta;
        if (enemyState == EnemyState.DEATH) {
            if (stateTimer >= DEATH_DURATION) {
                readyToBeRemoved = true;
            }
        }
    }

    public EnemyState getEnemyState() {
        return enemyState;
    }

    public void setEnemyState(EnemyState enemyState) {
        this.enemyState = enemyState;
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public int getSoulReward() {
        return soulReward;
    }

    public float getHurtStunned() {
        return hurtStunned;
    }

    public int getContactDamage() {
        return contactDamage;
    }

    public String getType() {
        return type;
    }




    public boolean isReadyToBeRemoved() {
        return readyToBeRemoved;
    }

    public void setReadyToBeRemoved(boolean readyToBeRemoved) {
        this.readyToBeRemoved = readyToBeRemoved;
    }
}
