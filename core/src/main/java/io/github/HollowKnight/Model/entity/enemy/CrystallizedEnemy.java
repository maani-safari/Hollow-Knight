package io.github.HollowKnight.Model.entity.enemy;

import com.badlogic.gdx.math.Rectangle;

public class CrystallizedEnemy extends Enemy {
    public static final float VISION_RANGE = 450f;
    public static final float VISION_EXTRA_HEIGHT = 20f;
    public static final float CHARGE_DURATION = 0.9f;
    public static final float FIRE_DURATION = 0.5f;
    public static final float LASER_RANGE = 1500f;
    public static final float LASER_HEIGHT = 30f;
    public static final float RECOIL_SPEED = 35f;

    public static final float ATTACK_COOLDOWN = 2.5f;
    public static final float ENRAGE_DURATION = 6f;
    public static final float ENRAGE_SPEED_FACTOR = 0.35f;
    public static final float ENRAGE_RUN_SPEED = 400f;
    public static final float ENRAGE_STOP_RANGE = 140f;

    public static final float TURN_DURATION = 0.3f;

    public EnemyType enemyType;

    private enum SubPhase { CHARGE, FIRE }
    private enum EnragePhase { RUN, TURN, READY}

    private final boolean originalFacingRight;

    private boolean enrageRunActive;

    private SubPhase subPhase;
    private float subTimer;
    private float attackCooldown;
    private boolean enraged;
    private float enrageTimer;
    private EnragePhase enragePhase;
    private boolean pendingFaceRight;
    private float turnTimer;
    private final Rectangle laserHitbox = new Rectangle();


    public CrystallizedEnemy(float spawnX, float spawnY, int maxHp, int soulReward, int contactDamage,
                             boolean movingRight, EnemyType enemyType) {
        super(spawnX, spawnY, 80, 100, maxHp, soulReward, contactDamage);
        this.type = enemyType.name();
        this.enemyType = enemyType;
        this.originalFacingRight = movingRight;
        this.facingRight = movingRight;
        this.enemyState = EnemyState.IDLE;
    }

    @Override
    public void setEnemyState(EnemyState enemyState) {
        this.enemyState = enemyState;
        this.stateTimer = 0f;
    }

    @Override
    public void update(float delta, EnemyContext ctx) {
        if (attackCooldown > 0) attackCooldown -= delta;

        switch (enemyState) {
            case IDLE -> updateWatching(ctx);
            case ATTACKING -> updateAttacking(delta, ctx);
            case ALERT -> updateEnraged(delta, ctx);
            case HURT -> updateHurt();
            case DEATH -> velocity.set(0, 0);
            default -> {}
        }
    }

    private void updateWatching(EnemyContext ctx) {
        velocity.x = 0;
        facingRight = originalFacingRight;
        if (ctx.knightAlive && seesKnight(ctx) && attackCooldown <= 0) {
            startLaserCharge();
        }
    }

    private void updateAttacking(float delta, EnemyContext ctx) {
        if (enraged && ctx.knightAlive) {
            faceTowardKnight(ctx);
        } else {
            facingRight = originalFacingRight;
        }

        subTimer += delta;

        switch (subPhase) {
            case CHARGE -> {
                velocity.x = 0;
                if (subTimer >= chargeDuration()) {
                    subPhase = SubPhase.FIRE;
                    subTimer = 0f;
                }
            }

            case FIRE -> {
                velocity.x = facingRight ? -RECOIL_SPEED : RECOIL_SPEED;

                if (subTimer >= FIRE_DURATION) {
                    velocity.x = 0;

                    if (!enraged) {
                        beginEnrage();
                    } else {
                        attackCooldown = cooldownDuration();
                        enragePhase = EnragePhase.RUN;
                        setEnemyState(EnemyState.ALERT);
                    }
                }
            }
        }
    }

    private void beginEnrage() {
        enraged = true;
        enrageTimer = ENRAGE_DURATION;
        attackCooldown = 0f;
        enragePhase = EnragePhase.RUN;
        setEnemyState(EnemyState.ALERT);
    }

    private void updateEnraged(float delta, EnemyContext ctx) {
        enrageTimer -= delta;
        if (enrageTimer <= 0) {
            enraged = false;
            enragePhase = EnragePhase.READY;
            enrageRunActive = false;
            velocity.set(0, 0);
            facingRight = originalFacingRight;
            setEnemyState(EnemyState.IDLE);
            return;
        }
        if (!ctx.knightAlive) {
            velocity.x = 0;
            return;
        }

        switch (enragePhase) {
            case TURN -> updateTurn(delta);
            case RUN -> updateEnrageRun(ctx);
            case READY -> updateEnrageReady(ctx);
        }
    }

    private void updateEnrageRun(EnemyContext ctx) {
        enrageRunActive = false;
        float dist = ctx.distanceToKnight(position);
        if (dist <= ENRAGE_STOP_RANGE) {
            velocity.x = 0;
            enragePhase = EnragePhase.READY;
            return;
        }

        if (needsTurn(ctx)) {
            startTurn(ctx);
            return;
        }

        boolean knightLeft = ctx.knightIsLeftOf(position);
        facingRight = !knightLeft;
        velocity.x = knightLeft ? -ENRAGE_RUN_SPEED : ENRAGE_RUN_SPEED;
        enrageRunActive = true;
    }

    private void updateEnrageReady(EnemyContext ctx) {
        enrageRunActive = false;
        velocity.x = 0;

        if (needsTurn(ctx)) {
            startTurn(ctx);
            return;
        }

        faceTowardKnight(ctx);

        if (attackCooldown <= 0 && canHitKnightWhileEnraged(ctx)) {
            startLaserCharge();
            return;
        }

        if (ctx.distanceToKnight(position) > ENRAGE_STOP_RANGE + 40f) {
            enragePhase = EnragePhase.RUN;
        }
    }

    private void updateTurn(float delta) {
        enrageRunActive = false;
        velocity.x = 0;
        turnTimer += delta;
        if (turnTimer >= TURN_DURATION) {
            facingRight = pendingFaceRight;
            enragePhase = EnragePhase.READY;
            turnTimer = 0f;
        }
    }

    private void startTurn(EnemyContext ctx) {
        enrageRunActive = false;
        pendingFaceRight = wantFaceKnight(ctx);
        enragePhase = EnragePhase.TURN;
        turnTimer = 0f;
        stateTimer = 0f;
        velocity.x = 0;
    }

    private boolean needsTurn(EnemyContext ctx) {
        return wantFaceKnight(ctx) != facingRight;
    }

    private boolean wantFaceKnight(EnemyContext ctx) {
        float centerX = hitbox.x + hitbox.width / 2f;
        float dx = ctx.knightPosition.x - centerX;
        if (Math.abs(dx) < 8f) return facingRight;
        return dx > 0;
    }

    private void faceTowardKnight(EnemyContext ctx) {
        facingRight = wantFaceKnight(ctx);
    }

    public void onHitWall() {
        if (enraged && enragePhase == EnragePhase.RUN) {
            enrageRunActive = false;
            enragePhase = EnragePhase.READY;
            velocity.x = 0;
        }
    }

    private void updateHurt() {
        velocity.x = 0;
        if (stateTimer >= hurtStunned) {
            if (enraged) {
                setEnemyState(EnemyState.ALERT);
            } else {
                facingRight = originalFacingRight;
                setEnemyState(EnemyState.IDLE);
            }
        }
    }

    private void startLaserCharge() {
        subPhase = SubPhase.CHARGE;
        subTimer = 0f;
        velocity.x = 0;
        setEnemyState(EnemyState.ATTACKING);
    }

    private float chargeDuration() {
        return enraged ? CHARGE_DURATION * ENRAGE_SPEED_FACTOR : CHARGE_DURATION;
    }

    private float cooldownDuration() {
        return enraged ? ATTACK_COOLDOWN * ENRAGE_SPEED_FACTOR : ATTACK_COOLDOWN;
    }

    private boolean seesKnight(EnemyContext ctx) {
        float x = facingRight ? hitbox.x + hitbox.width : hitbox.x - VISION_RANGE;
        Rectangle vision = new Rectangle(x, hitbox.y,
            VISION_RANGE, hitbox.height + VISION_EXTRA_HEIGHT);
        return vision.contains(ctx.knightPosition);
    }

    private boolean canHitKnightWhileEnraged(EnemyContext ctx) {
        float x = facingRight ? hitbox.x + hitbox.width : hitbox.x - LASER_RANGE;
        Rectangle range = new Rectangle(x, hitbox.y - 80f, LASER_RANGE, hitbox.height + 160f);
        return range.contains(ctx.knightPosition);
    }

    public boolean isCharging() {
        return enemyState == EnemyState.ATTACKING && subPhase == SubPhase.CHARGE;
    }

    public float getChargeProgress() {
        return Math.min(1f, subTimer / chargeDuration());
    }

    public boolean isFiringLaser() {
        return enemyState == EnemyState.ATTACKING && subPhase == SubPhase.FIRE;
    }

    public boolean isEnraged() {
        return enraged;
    }

    public boolean isTurning() {
        return enraged && enragePhase == EnragePhase.TURN;
    }

    public boolean isEnrageRunning() {
        return enraged && enragePhase == EnragePhase.RUN && enrageRunActive;
    }

    public Rectangle getLaserHitbox() {
        float x = facingRight ? hitbox.x + hitbox.width : hitbox.x - LASER_RANGE;
        float y = hitbox.y + hitbox.height / 2f - LASER_HEIGHT / 2f;
        laserHitbox.set(x, y, LASER_RANGE, LASER_HEIGHT);
        return laserHitbox;
    }
}
