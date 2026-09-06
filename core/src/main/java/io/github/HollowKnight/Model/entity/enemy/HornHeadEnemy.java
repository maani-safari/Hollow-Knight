package io.github.HollowKnight.Model.entity.enemy;

import com.badlogic.gdx.math.Rectangle;

public class HornHeadEnemy extends GroundEnemy {
    public static final float PATROL_SPEED = 80f;
    public static final float VISION_WIDTH = 200f;
    public static final float VISION_HEIGHT = 70f;
    public static final float WINDUP_DURATION = 0.5f;
    public static final float CHARGE_SPEED = 280f;
    public static final float CHARGE_DURATION = 1.2f;
    public static final float RECOVERY_DURATION = 0.5f;
    public static final float ATTACK_COOLDOWN = 1.5f;

    private enum AttackPhase { WINDUP, DASHING, RECOVERING }

    private AttackPhase attackPhase;
    private float attackTimer;
    private float attackCooldown;
    private float chargeDir;

    public HornHeadEnemy(float spawnX, float spawnY, int maxHp, int soulReward, int contactDamage,
                         boolean movingRight, EnemyType enemyType) {
        super(spawnX, spawnY, maxHp, soulReward, contactDamage, movingRight, enemyType);
        hitbox.setSize(80, 100);
    }

    @Override
    protected float getPatrolSpeed() {
        return PATROL_SPEED;
    }

    @Override
    public void update(float delta, EnemyContext ctx) {
        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }

        switch (enemyState) {
            case ATTACKING -> updateAttacking(delta);
            case WALK -> {
                if (attackCooldown <= 0 && canSeeKnight(ctx)) {
                    startWindUp();
                } else {
                    updatePatrol(delta, ctx);
                }
            }
            case IDLE -> {
                if (attackCooldown <= 0 && canSeeKnight(ctx)) {
                    startWindUp();
                } else {
                    updateIdleRest(delta);
                }
            }
            case HURT -> updateHurt(delta);
            case DEATH -> velocity.set(0, 0);
            default -> {}
        }
    }

    private void startWindUp() {
        attackPhase = AttackPhase.WINDUP;
        attackTimer = 0f;
        chargeDir = facingRight ? 1f : -1f;
        velocity.x = 0;
        setEnemyState(EnemyState.ATTACKING);
    }

    private void updateAttacking(float delta) {
        attackTimer += delta;
        switch (attackPhase) {
            case WINDUP -> {
                velocity.x = 0;
                if (attackTimer >= WINDUP_DURATION) {
                    attackPhase = AttackPhase.DASHING;
                    attackTimer = 0f;
                    stateTimer = 0f;
                }
            }
            case DASHING -> {
                facingRight = chargeDir > 0;
                velocity.x = chargeDir * CHARGE_SPEED;
                if (attackTimer >= CHARGE_DURATION) {
                    beginRecovery();
                }
            }
            case RECOVERING -> {
                velocity.x = 0;
                if (attackTimer >= RECOVERY_DURATION) {
                    attackCooldown = ATTACK_COOLDOWN;
                    setEnemyState(EnemyState.WALK);
                }
            }
        }
    }

    private void beginRecovery() {
        attackPhase = AttackPhase.RECOVERING;
        attackTimer = 0f;
        velocity.x = 0;
    }

    private boolean canSeeKnight(EnemyContext ctx) {
        if (!ctx.knightAlive) {
            return false;
        }
        Rectangle vision = getVisionBox();
        return vision.contains(ctx.knightPosition.x, ctx.knightPosition.y);
    }

    private Rectangle getVisionBox() {
        float x = facingRight ? position.x + hitbox.width : position.x - VISION_WIDTH;
        return new Rectangle(x, position.y, VISION_WIDTH, VISION_HEIGHT);
    }

    @Override
    public void onHitWall() {
        if (enemyState == EnemyState.ATTACKING && attackPhase == AttackPhase.DASHING) {
            beginRecovery();
            return;
        }
        super.onHitWall();
    }

    @Override
    public void onCLif() {
        if (enemyState == EnemyState.ATTACKING && attackPhase == AttackPhase.DASHING) {
            beginRecovery();
            return;
        }
        super.onCLif();
    }

    public void onChargeHitPlayer() {
        if (attackPhase == AttackPhase.DASHING) {
            beginRecovery();
        }
    }

    public boolean isWindingUp() {
        return enemyState == EnemyState.ATTACKING && attackPhase == AttackPhase.WINDUP;
    }

    public boolean isDashing() {
        return enemyState == EnemyState.ATTACKING && attackPhase == AttackPhase.DASHING;
    }

    public float getCliffProbe() {
        return isDashing() ? CHARGE_SPEED * 0.05f : 0f;
    }
}
