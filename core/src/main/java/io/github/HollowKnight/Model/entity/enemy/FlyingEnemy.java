package io.github.HollowKnight.Model.entity.enemy;

import com.badlogic.gdx.math.Vector2;

public class FlyingEnemy extends Enemy{
    public  static final float  SPOT_ZONE = 350f;
    public static final float DIVE_ZONE = 150f;
    public static final float  DIVE_SPEED = 500f;
    public static final float HOVER_SPEED = 100f;
    public static final float ATTACK_DURATION = 0.5f;
    public static final float ATTACK_COOLDOWN = 1f;
    public static final float PREPARING_DURATION = 0.5f;
    public EnemyType enemyType;
    private float attackingCooldownTimer;
    private Vector2 diveTarget;


    public FlyingEnemy(float spawnX, float spawnY, int maxHp, int soulReward, int contactDamage , boolean facingRight , EnemyType enemyType) {
        super(spawnX, spawnY, 80, 50, maxHp, soulReward, contactDamage);
        this.enemyState = EnemyState.IDLE;
        this.facingRight = facingRight;
        this.diveTarget = new Vector2();
        this.type = enemyType.name();
    }

    @Override
    public void update(float delta, EnemyContext ctx) {

        stateTimer += delta;
        if (attackingCooldownTimer > 0) {
            attackingCooldownTimer = Math.max(0f, attackingCooldownTimer - delta);
        }
        switch (enemyState){
            case IDLE -> updateIdle(ctx);
            case HURT -> updateHurt();
            case ALERT -> updateAlert(ctx);
            case ATTACKING -> updateAttack();
            case DEATH -> velocity.set(0 , 0);
        }
    }
    public void updateHurt(){
        velocity.set(0 , 0);
        if (stateTimer >= hurtStunned){
            setEnemyState(EnemyState.IDLE);
        }
    }
    public void updateAttack(){

        Vector2 dir = diveTarget.cpy().sub(position);
        dir.nor();
        velocity.set(dir.x * DIVE_SPEED, dir.y * DIVE_SPEED);
        if (stateTimer >= ATTACK_DURATION){
            attackingCooldownTimer = ATTACK_COOLDOWN;
            setEnemyState(EnemyState.IDLE);
        }
    }
    public void updateIdle(EnemyContext ctx){
        if (!ctx.knightAlive) {
            velocity.set(0, (float) Math.sin(stateTimer * 1.5f) * 20f);
            return;
        }

        float distance = ctx.distanceToKnight(position);

        if (distance <= SPOT_ZONE && attackingCooldownTimer <= 0) {

            facingRight = !ctx.knightIsLeftOf(position);

            if (distance <= DIVE_ZONE) {
                setEnemyState(EnemyState.ALERT);
                return;
            }
            Vector2 chaseDir = ctx.knightPosition.cpy().sub(position).nor();
            float hoverBobbing = (float) Math.sin(stateTimer * 5f) * 15f;
            velocity.set(chaseDir.x * HOVER_SPEED, (chaseDir.y * HOVER_SPEED) + hoverBobbing);

        } else {
            velocity.set(0, (float) Math.sin(stateTimer * 1.5f) * HOVER_SPEED * 0.3f);
        }
    }
    public void updateAlert( EnemyContext ctx){
        velocity.set(0,0);
        facingRight = !ctx.knightIsLeftOf(position);
        if (stateTimer >= PREPARING_DURATION){
            diveTarget.set(ctx.knightPosition);
            setEnemyState(EnemyState.ATTACKING);
        }
    }

    public EnemyType getEnemyType() {
        return enemyType;
    }

    public void setEnemyType(EnemyType enemyType) {
        this.enemyType = enemyType;
    }

    @Override
    public void setEnemyState(EnemyState enemyState) {
        this.enemyState = enemyState;
        this.stateTimer = 0;

    }
}
