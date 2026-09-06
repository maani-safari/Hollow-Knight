package io.github.HollowKnight.Model.entity.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class FalseKnight extends Enemy {
    public static final int DEFAULT_MAX_HP = 300;
    public static final int DEFAULT_SOUL_REWARD = 50;
    public static final int DEFAULT_CONTACT_DAMAGE = 2;
    public static final float BOSS_WIDTH = 320f;
    public static final float BOSS_HEIGHT = 400f;

    public static final float WALK_SPEED = 160f;
    public static final float RUN_SPEED = 160f;
    public static final float CHARGE_SPEED = 280f;
    public static final float LEAP_SPEED_X = 220f;
    public static final float LEAP_SPEED_Y = 620f;
    public static final float DEFENSIVE_LEAP_SPEED_X = 200f;
    public static final float DEFENSIVE_LEAP_SPEED_Y = 480f;

    public static final float CLOSE_RANGE = 160f;
    public static final float FAR_RANGE = 420f;

    public static final float STUN_DURATION = 4.5f;
    public static final float DEATH_ANIM_DURATION = 3.5f;
    public static final int DEFENSIVE_HIT_THRESHOLD = 3;
    public static final float DEFENSIVE_HIT_WINDOW = 2.0f;

    public static final float MACE_ANTIC_DURATION = 0.55f;
    public static final float MACE_ATTACK_DURATION = 0.35f;
    public static final float MACE_RECOVER_DURATION = 0.45f;
    public static final float CHARGE_WINDUP_DURATION = 0.4f;
    public static final float CHARGE_DURATION = 1.1f;
    public static final float CHARGE_RECOVER_DURATION = 0.5f;
    public static final float LEAP_ASCENT_DURATION = 0.35f;
    public static final float LEAP_DESCENT_DURATION = 0.4f;
    public static final float SHOCK_JUMP_DURATION = 0.45f;
    public static final float SHOCK_LAND_DURATION = 0.35f;
    public static final float DEFENSIVE_LEAP_DURATION = 0.55f;

    public static final float SHOCKWAVE_WIDTH = 140f;
    public static final float SHOCKWAVE_HEIGHT = 150f;
    public static final float SHOCKWAVE_LIFETIME = 2.5f;

    private FalseKnightState aiState = FalseKnightState.IDLE;
    private int macePhase;
    private int chargePhase;
    private int leapPhase;
    private int shockPhase;
    private float actionTimer;
    private float decisionTimer;
    private float stunHpThreshold;
    private boolean phase2;
    private boolean stunTriggered;
    private boolean weakSpotActive;
    private float speedMultiplier = 1f;
    private float attackSpeedMultiplier = 1f;
    private float decisionInterval = 1.2f;
    private float chargeDirection = 1f;
    private Vector2 leapTarget = new Vector2();
    private final Rectangle attackHitbox = new Rectangle();
    private final Rectangle weakSpotHitbox = new Rectangle();
    private final Array<Shockwave> shockwaves = new Array<>();
    private final float[] recentHitTimes = new float[DEFENSIVE_HIT_THRESHOLD];
    private int recentHitIndex;
    private int recentHitCount;
    private float hitWindowTimer;
    private FalseKnightAttack lastAttack = null;
    private boolean grounded;
    private boolean heavyImpactPending;

    public FalseKnight(float spawnX, float spawnY) {
        this(spawnX, spawnY, DEFAULT_MAX_HP, DEFAULT_SOUL_REWARD, DEFAULT_CONTACT_DAMAGE);
    }

    public FalseKnight(float spawnX, float spawnY, int maxHp, int soulReward, int contactDamage) {
        super(spawnX, spawnY, BOSS_WIDTH, BOSS_HEIGHT, maxHp, soulReward, contactDamage);
        this.type = EnemyType.FALSE_KNIGHT.name();
        this.enemyState = EnemyState.IDLE;
        this.hurtStunned = 0.15f;
        this.stunHpThreshold = maxHp / 2f;
        facingRight = false;
    }

    @Override
    public void setEnemyState(EnemyState enemyState) {
        this.enemyState = enemyState;
        this.stateTimer = 0f;
    }

    public void enterAiState(FalseKnightState state) {
        this.aiState = state;
        this.actionTimer = 0f;
        this.stateTimer = 0f;
        attackHitbox.set(0, 0, 0, 0);

        switch (state) {
            case IDLE -> {
                velocity.set(0, 0);
                setEnemyState(EnemyState.IDLE);
            }
            case WALK -> setEnemyState(EnemyState.WALK);
            case RUNNING_CHARGE -> {
                setEnemyState(EnemyState.ATTACKING);
                chargePhase = 0;
            }
            case JUMP, DEFENSIVE_LEAP -> {
                setEnemyState(EnemyState.ATTACKING);
                leapPhase = 0;
            }
            case MACE_SLAM -> {
                setEnemyState(EnemyState.ATTACKING);
                macePhase = 0;
                velocity.set(0, 0);
            }
            case SHOCKWAVE_LANDING -> {
                setEnemyState(EnemyState.ATTACKING);
                shockPhase = 0;
            }
            case STUNNED -> {
                velocity.set(0, 0);
                setEnemyState(EnemyState.HURT);
                weakSpotActive = true;
            }
            case DEAD -> {
                velocity.set(0, 0);
                setEnemyState(EnemyState.DEATH);
                weakSpotActive = false;
            }
            default -> {}
        }
    }

    @Override
    public void update(float delta, EnemyContext ctx) {
        if (aiState == FalseKnightState.DEAD) {
            velocity.set(0, 0);
            return;
        }

        if (aiState == FalseKnightState.STUNNED) {
            velocity.set(0, 0);
            updateWeakSpot();
            return;
        }

        updateHitWindow(delta);

        switch (aiState) {
            case IDLE -> velocity.x = 0;
            case WALK -> updateWalk(ctx);
            case RUNNING_CHARGE -> updateRunningCharge(delta);
            case JUMP -> updateOffensiveLeap(delta, ctx);
            case MACE_SLAM -> updateMaceSlam(delta);
            case SHOCKWAVE_LANDING -> updateShockwaveLanding(delta, ctx);
            case DEFENSIVE_LEAP -> updateDefensiveLeap(delta);
            default -> {}
        }

        updateShockwaves(delta);
        updateWeakSpot();
    }

    private void updateWalk(EnemyContext ctx) {
        if (!ctx.knightAlive) {
            velocity.x = 0;
            return;
        }
        facingRight = ctx.knightPosition.x >= position.x;
        float speed = WALK_SPEED * speedMultiplier;
        velocity.x = facingRight ? speed : -speed;
    }

    private void updateRunningCharge(float delta) {
        actionTimer += delta;
        float antic = CHARGE_WINDUP_DURATION / attackSpeedMultiplier;
        float charge = CHARGE_DURATION / attackSpeedMultiplier;
        float recover = CHARGE_RECOVER_DURATION / attackSpeedMultiplier;

        switch (chargePhase) {
            case 0 -> {
                velocity.set(0, 0);
                if (actionTimer >= antic) {
                    chargePhase = 1;
                    actionTimer = 0f;
                    stateTimer = 0f;
                }
            }
            case 1 -> {
                velocity.x = chargeDirection * CHARGE_SPEED * speedMultiplier;
                facingRight = chargeDirection > 0;
                attackHitbox.set(
                    facingRight ? hitbox.x + hitbox.width * 0.2f : hitbox.x - hitbox.width * 0.5f,
                    hitbox.y,
                    hitbox.width * 0.7f,
                    hitbox.height * 0.85f
                );
                if (actionTimer >= charge) {
                    chargePhase = 2;
                    actionTimer = 0f;
                    stateTimer = 0f;
                    velocity.x = 0;
                    attackHitbox.set(0, 0, 0, 0);
                }
            }
            case 2 -> {
                velocity.x = 0;
                if (actionTimer >= recover) {
                    enterAiState(FalseKnightState.IDLE);
                }
            }
        }
    }

    private void updateOffensiveLeap(float delta, EnemyContext ctx) {
        actionTimer += delta;
        float ascent = LEAP_ASCENT_DURATION / attackSpeedMultiplier;
        float descent = LEAP_DESCENT_DURATION / attackSpeedMultiplier;

        if (leapPhase == 0) {
            float dir = leapTarget.x >= position.x ? 1f : -1f;
            facingRight = dir > 0;
            velocity.x = dir * LEAP_SPEED_X * speedMultiplier;
            velocity.y = LEAP_SPEED_Y;
            leapPhase = 1;
            actionTimer = 0f;
            stateTimer = 0f;
        } else if (leapPhase == 1) {
            if (actionTimer >= ascent) {
                leapPhase = 2;
                actionTimer = 0f;
                stateTimer = 0f;
            }
        } else if (leapPhase == 2) {
            velocity.x *= 0.98f;
            if (grounded && actionTimer >= descent * 0.2f) {
                velocity.set(0, 0);
                enterAiState(FalseKnightState.IDLE);
            }
        }
    }

    private void updateMaceSlam(float delta) {
        actionTimer += delta;
        float antic = MACE_ANTIC_DURATION / attackSpeedMultiplier;
        float attack = MACE_ATTACK_DURATION / attackSpeedMultiplier;
        float recover = MACE_RECOVER_DURATION / attackSpeedMultiplier;

        switch (macePhase) {
            case 0 -> {
                velocity.set(0, 0);
                if (actionTimer >= antic) {
                    macePhase = 1;
                    actionTimer = 0f;
                    stateTimer = 0f;
                    markHeavyImpact();
                }
            }
            case 1 -> {


                velocity.set(0, 0);
                float reach = hitbox.width * 0.9f;
                attackHitbox.set(
                    facingRight ? hitbox.x + hitbox.width * 0.15f : hitbox.x - reach * 0.85f,
                    hitbox.y,
                    reach,
                    hitbox.height
                );
                if (actionTimer >= attack) {
                    macePhase = 2;
                    actionTimer = 0f;
                    stateTimer = 0f;
                    attackHitbox.set(0, 0, 0, 0);
                }
            }
            case 2 -> {
                velocity.set(0, 0);
                if (actionTimer >= recover) {
                    enterAiState(FalseKnightState.IDLE);
                }
            }
        }
    }

    private void updateShockwaveLanding(float delta, EnemyContext ctx) {
        actionTimer += delta;
        float jump = SHOCK_JUMP_DURATION / attackSpeedMultiplier;
        float land = SHOCK_LAND_DURATION / attackSpeedMultiplier;

        switch (shockPhase) {
            case 0 -> {
                float dir = ctx.knightPosition.x >= position.x ? 1f : -1f;
                facingRight = dir > 0;
                velocity.x = dir * LEAP_SPEED_X * 0.8f * speedMultiplier;
                velocity.y = LEAP_SPEED_Y * 0.9f;
                if (actionTimer >= jump) {
                    shockPhase = 1;
                    actionTimer = 0f;
                    stateTimer = 0f;
                }
            }
            case 1 -> {
                velocity.x *= 0.95f;
                if (grounded) {
                    shockPhase = 2;
                    actionTimer = 0f;
                    stateTimer = 0f;
                    spawnShockwave();
                    velocity.set(0, 0);
                }
            }
            case 2 -> {
                velocity.set(0, 0);
                if (actionTimer >= land) {
                    enterAiState(FalseKnightState.IDLE);
                }
            }
        }
    }

    private void updateDefensiveLeap(float delta) {
        actionTimer += delta;
        if (actionTimer < 0.08f) {
            velocity.y = DEFENSIVE_LEAP_SPEED_Y;
            velocity.x = (facingRight ? -1f : 1f) * DEFENSIVE_LEAP_SPEED_X * speedMultiplier;
        } else if (grounded && actionTimer >= DEFENSIVE_LEAP_DURATION) {
            velocity.set(0, 0);
            enterAiState(FalseKnightState.IDLE);
        }
    }

    private void markHeavyImpact() {
        heavyImpactPending = true;
    }

    public boolean consumeHeavyImpact() {
        if (!heavyImpactPending) {
            return false;
        }
        heavyImpactPending = false;
        return true;
    }

    private void spawnShockwave() {
        markHeavyImpact();
        Shockwave wave = new Shockwave();
        wave.active = true;
        wave.facingRight = facingRight;
        wave.x = facingRight ? hitbox.x + hitbox.width * 0.5f : hitbox.x - SHOCKWAVE_WIDTH * 0.5f;
        wave.y = hitbox.y;
        wave.width = SHOCKWAVE_WIDTH;
        wave.height = SHOCKWAVE_HEIGHT;
        wave.speed = 260f * speedMultiplier;
        wave.maxLifetime = SHOCKWAVE_LIFETIME;
        wave.lifetime = SHOCKWAVE_LIFETIME;
        wave.hitbox = new Rectangle(wave.x, wave.y, wave.width, wave.height);
        shockwaves.add(wave);
    }

    private void updateShockwaves(float delta) {
        for (int i = shockwaves.size - 1; i >= 0; i--) {
            Shockwave wave = shockwaves.get(i);
            if (!wave.active) {
                shockwaves.removeIndex(i);
                continue;
            }
            wave.lifetime -= delta;
            if (wave.lifetime <= 0f) {
                wave.active = false;
                continue;
            }
            float dir = wave.facingRight ? 1f : -1f;
            wave.x += dir * wave.speed * delta;
            wave.hitbox.set(wave.x, wave.y, wave.width, wave.height);
        }
    }

    private void updateWeakSpot() {
        if (!weakSpotActive) {
            weakSpotHitbox.set(0, 0, 0, 0);
            return;
        }
        float w = hitbox.width * 0.35f;
        float h = hitbox.height * 0.3f;
        float x = hitbox.x + hitbox.width * 0.32f;
        float y = hitbox.y + hitbox.height * 0.45f;
        weakSpotHitbox.set(x, y, w, h);
    }

    private void updateHitWindow(float delta) {
        if (hitWindowTimer > 0f) {
            hitWindowTimer -= delta;
            if (hitWindowTimer <= 0f) {
                recentHitCount = 0;
            }
        }
    }

    @Override
    public void onHit(int amount) {
        if (aiState == FalseKnightState.DEAD) return;

        if (aiState == FalseKnightState.STUNNED) {
            takeDamage(amount);
            if (!isAlive()) {
                beginDeath();
            }
            return;
        }

        takeDamage(amount);
        recordHit();

        if (!stunTriggered && currentHp <= stunHpThreshold) {
            beginStun();
            return;
        }

        if (!isAlive()) {
            beginDeath();
            return;
        }

        if (shouldDefensiveLeap()) {
            beginDefensiveLeap();
            return;
        }

        if (aiState != FalseKnightState.MACE_SLAM
            && aiState != FalseKnightState.RUNNING_CHARGE
            && aiState != FalseKnightState.SHOCKWAVE_LANDING
            && aiState != FalseKnightState.JUMP
            && aiState != FalseKnightState.DEFENSIVE_LEAP) {
            enemyState = EnemyState.HURT;
            stateTimer = 0f;
            velocity.x = 0;
        }
    }

    private void recordHit() {
        if (hitWindowTimer <= 0f) {
            recentHitCount = 0;
        }
        hitWindowTimer = DEFENSIVE_HIT_WINDOW;
        recentHitTimes[recentHitIndex] = DEFENSIVE_HIT_WINDOW;
        recentHitIndex = (recentHitIndex + 1) % DEFENSIVE_HIT_THRESHOLD;
        recentHitCount++;
    }

    private boolean shouldDefensiveLeap() {
        return recentHitCount >= DEFENSIVE_HIT_THRESHOLD
            && aiState != FalseKnightState.STUNNED
            && aiState != FalseKnightState.DEFENSIVE_LEAP;
    }

    public void beginStun() {
        stunTriggered = true;
        enterAiState(FalseKnightState.STUNNED);
    }

    public void beginDeath() {
        enterAiState(FalseKnightState.DEAD);
        readyToBeRemoved = false;
    }

    public void beginDefensiveLeap() {
        recentHitCount = 0;
        enterAiState(FalseKnightState.DEFENSIVE_LEAP);
    }

    public void enterPhase2() {
        phase2 = true;
        speedMultiplier = 1.35f;
        attackSpeedMultiplier = 1.3f;
        decisionInterval = 0.75f;
    }

    public void endStun() {
        weakSpotActive = false;
        enterAiState(FalseKnightState.IDLE);
    }

    public void prepareRunningCharge(float direction) {
        chargeDirection = direction;
        lastAttack = FalseKnightAttack.RUNNING_CHARGE;
        enterAiState(FalseKnightState.RUNNING_CHARGE);
    }

    public void prepareMaceSlam() {
        lastAttack = FalseKnightAttack.MACE_SLAM;
        enterAiState(FalseKnightState.MACE_SLAM);
    }

    public void prepareOffensiveLeap(Vector2 target) {
        leapTarget.set(target);
        lastAttack = FalseKnightAttack.OFFENSIVE_LEAP;
        enterAiState(FalseKnightState.JUMP);
    }

    public void prepareShockwaveLanding() {
        lastAttack = FalseKnightAttack.SHOCKWAVE_LANDING;
        enterAiState(FalseKnightState.SHOCKWAVE_LANDING);
    }

    public void prepareWalk() {
        enterAiState(FalseKnightState.WALK);
    }

    public void prepareIdle() {
        enterAiState(FalseKnightState.IDLE);
    }

    public void onChargeBlocked() {
        if (aiState == FalseKnightState.RUNNING_CHARGE && chargePhase == 1) {
            chargePhase = 2;
            actionTimer = 0f;
            stateTimer = 0f;
            velocity.x = 0;
            attackHitbox.set(0, 0, 0, 0);
        }
    }

    @Override
    public void tickState(float delta) {
        stateTimer += delta;
        if (aiState == FalseKnightState.DEAD && stateTimer >= DEATH_ANIM_DURATION) {
            readyToBeRemoved = true;
        }
    }

    public boolean isActionComplete() {
        return aiState == FalseKnightState.IDLE;
    }

    public boolean canDecide(float delta) {
        if (aiState == FalseKnightState.STUNNED || aiState == FalseKnightState.DEAD) {
            return false;
        }
        if (aiState != FalseKnightState.IDLE && aiState != FalseKnightState.WALK) {
            return false;
        }
        decisionTimer -= delta;
        return decisionTimer <= 0f;
    }

    public void resetDecisionTimer() {
        decisionTimer = decisionInterval;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public FalseKnightState getAiState() {
        return aiState;
    }

    public boolean isPhase2() {
        return phase2;
    }

    public boolean isStunTriggered() {
        return stunTriggered;
    }

    public boolean isWeakSpotActive() {
        return weakSpotActive;
    }

    public Rectangle getWeakSpotHitbox() {
        return weakSpotHitbox;
    }

    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }

    public Array<Shockwave> getShockwaves() {
        return shockwaves;
    }

    public FalseKnightAttack getLastAttack() {
        return lastAttack;
    }

    public int getMacePhase() {
        return macePhase;
    }

    public int getChargePhase() {
        return chargePhase;
    }

    public int getLeapPhase() {
        return leapPhase;
    }

    public int getShockPhase() {
        return shockPhase;
    }

    public float getActionTimer() {
        return actionTimer;
    }

    public static class Shockwave {
        public boolean active;
        public boolean facingRight;
        public float x;
        public float y;
        public float width;
        public float height;
        public float speed;
        public float maxLifetime;
        public float lifetime;
        public Rectangle hitbox;
    }
}
