package io.github.HollowKnight.Model.entity.enemy;

import static io.github.HollowKnight.Model.entity.enemy.EnemyState.*;

public class GroundEnemy extends Enemy {
    public static final float PATROL_SPEED = 100f;
    public static final float PATROL_DURATION = 3f;
    public static final float IDLE_DURATION = 1f;
    public static final float CLIF_DEPTH = 25f;

    public EnemyType enemyType;
    public boolean movingRight;
    private float patrolTimer;

    public GroundEnemy(float spawnX, float spawnY, int maxHp, int soulReward, int contactDamage,
                       boolean movingRight, EnemyType enemyType) {
        super(spawnX, spawnY, 70, 50, maxHp, soulReward, contactDamage);
        this.movingRight = movingRight;
        this.enemyState = WALK;
        this.type = enemyType.name();
        this.enemyType = enemyType;
    }

    @Override
    public void setEnemyState(EnemyState enemyState) {
        this.enemyState = enemyState;
        this.stateTimer = 0f;
    }

    @Override
    public void update(float delta, EnemyContext ctx) {
        switch (enemyState) {
            case WALK -> updatePatrol(delta, ctx);
            case IDLE -> updateIdleRest(delta);
            case HURT -> updateHurt(delta);
            case DEATH -> velocity.set(0, 0);
            default -> {}
        }
    }

    protected void updateIdleRest(float delta) {
        velocity.x = 0;
        if (stateTimer >= IDLE_DURATION) {
            setEnemyState(WALK);
        }
    }

    public void updateHurt(float delta) {
        velocity.x = 0;
        if (stateTimer >= hurtStunned) {
            setEnemyState(WALK);
        }
    }

    public void updatePatrol(float delta, EnemyContext ctx) {
        patrolTimer += delta;
        if (patrolTimer >= PATROL_DURATION) {
            patrolTimer = 0f;
            setEnemyState(IDLE);
            velocity.x = 0;
            return;
        }
        float speed = getPatrolSpeed();
        velocity.x = movingRight ? speed : -speed;
        facingRight = movingRight;
    }

    protected float getPatrolSpeed() {
        return PATROL_SPEED;
    }

    public void onHitWall() {
        movingRight = !movingRight;
        patrolTimer = 0f;
    }

    public void onCLif() {
        movingRight = !movingRight;
        patrolTimer = 0f;
    }

    public boolean isMovingRight() {
        return movingRight;
    }

    public EnemyType getEnemyType() {
        return enemyType;
    }
}
