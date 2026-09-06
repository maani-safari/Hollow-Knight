package io.github.HollowKnight.Model.entity;

import com.badlogic.gdx.math.Vector2;
import io.github.HollowKnight.Model.SoulVessel;
import io.github.HollowKnight.Model.charm.Charm;
import io.github.HollowKnight.Model.charm.CharmSlots;
import io.github.HollowKnight.Model.charm.CharmState;

public class Knight extends Entity{
    public static final int DEFAULT_MAX_MASKS = 5;
    public static final float INVINCIBILITY_DURATION = 1.0f;
    public static final float FOCUS_DURATION = 1.5f;
    public static final float HURT_DURATION = 0.4f;
    public static final float DASH_DURATION = 0.15f;
    public static final float DASH_COOLDOWN_BASE = 0.6f;
    public  final float WALL_SLIDE_GRAVITY = -30f;
    public static final int   BASE_NAIL_DAMAGE = 5;
    public static final float HURT_KNOCKBACK_X = 150f;
    public static final float HURT_KNOCKBACK_Y = 200f;

    private int healthMasks;
    private int maxHealthMasks;

    private boolean invincible;
    private float invincibilityTimer;

    private boolean ground;
    private boolean doubleJump;
    private boolean isCurrentlyDoubleJumping = false;
    private boolean jumpHigher;
    private boolean pogoJump;
    private boolean wallSliding;
    private boolean touchingWallLeft;
    private boolean touchingWallRight;

    private boolean focus;
    private float focusTimer;

    private boolean canDash;
    private boolean dashing;
    private float   dashTimer;
    private float   dashCooldownTimer;
    private Direction dashDirection;

    private boolean useAltSlash = false;
    private boolean attacking;
    private float   attackTimer;
    private float   attackCooldownTimer;
    private Direction attackDirection;

    private boolean hurt;
    private float   hurtTimer;

    private CharmSlots charmSlots;
    private CharmState charmState;

    private float spawnX;
    private float spawnY;
    private float lastSafeX;
    private float lastSafeY;
    private MovementState movementState;

    private boolean godMode;
    private boolean noclip;
    private boolean dialogueLocked;

    private int nailDamage;

    private SoulVessel soulVessel;

    public Knight(float spawnX , float spawnY) {
        super(spawnX, spawnY ,70, 100 , DEFAULT_MAX_MASKS );
        this.healthMasks    = DEFAULT_MAX_MASKS;
        this.maxHealthMasks = DEFAULT_MAX_MASKS;

        this.spawnX    = spawnX;
        this.spawnY    = spawnY;
        this.lastSafeX = spawnX;
        this.lastSafeY = spawnY;

        this.soulVessel   = new SoulVessel(0);
        this.soulVessel.fillMax();
        this.charmSlots   = new CharmSlots();
        this.charmState  = new CharmState();

        this.movementState = MovementState.IDLE;
        this.doubleJump = false;
        this.canDash       = true;
        this.nailDamage    = BASE_NAIL_DAMAGE;

        resetFlags();
    }
    public boolean applyDamage(int amount){
        if (godMode || !alive || invincible)
            return false;
        int adjusted = Math.max(1, Math.round(amount * charmState.incomingDamageMultiplier));
        healthMasks = Math.max(0 , healthMasks - adjusted);

        invincible = true;
        invincibilityTimer = INVINCIBILITY_DURATION;

        hurt = true;
        hurtTimer = HURT_DURATION;
        cancelFocus();
        if (healthMasks == 0){
            alive = false;
            movementState = MovementState.DEAD;
        }else
            movementState = MovementState.HURT;
        return true;
    }
    public boolean applyKnockBack(int amount , float sourceX){
        boolean damaged = applyDamage(amount);
        if (damaged) {
            float dir = position.x > sourceX ? 1f : -1f;
            velocity.set(dir * HURT_KNOCKBACK_X, HURT_KNOCKBACK_Y);
        }
        return damaged;
    }
    public boolean healMask(){
        if (healthMasks >= maxHealthMasks)
            return false;
        if (!soulVessel.consume(SoulVessel.FOCUS_COST))
            return false;
        healthMasks++;
        return true;
    }
    public int gainSoul() {
        int amount = SoulVessel.GAIN_PER_HIT + charmState.bonusSoulPerHit;
        return soulVessel.gain(amount);
    }
    public void startFocus() {
        if (!ground || focus || hurt || dashing || attacking) return;
        if (!soulVessel.canFocus()) return;
        focus   = true;
        velocity.set(0, 0);
        focusTimer = 0f;
        movementState = MovementState.FOCUSING;
    }

    public void cancelFocus() {
        if (!focus) return;
        focus  = false;
        focusTimer = 0f;
    }
    public boolean tickFocus(float delta) {
        if (!focus) return false;
        float speed = 1.0f / charmState.focusSpeedMultiplier;
        focusTimer += delta * speed;
        if (focusTimer >= FOCUS_DURATION) {
            focus   = false;
            focusTimer = 0f;
            return healMask();
        }
        return false;
    }
    public boolean canDashStart(){
        return canDash && !dashing && !attacking && !hurt && dashCooldownTimer <= 0 && alive;
    }
    public void startDash(Direction dir){
        dashing = true;
        dashTimer = 0f;
        dashDirection = dir;
        dashCooldownTimer = DASH_COOLDOWN_BASE * charmState.dashCooldownMultiplier;
        velocity.y = 0f;
        movementState = MovementState.DASHING;
        canDash = false;
        cancelFocus();
    }
    public void tickDash(float delta) {
        if (!dashing) return;
        dashTimer += delta;
        if (dashTimer >= DASH_DURATION) {
            dashing   = false;
            dashTimer = 0f;
        }
    }

    public void tickDashCooldown(float delta) {
        if (dashCooldownTimer > 0) {
            dashCooldownTimer = Math.max(0, dashCooldownTimer - delta);
        }
    }
    public boolean canJump(){
        return ground || Math.abs(velocity.y) < 1f;
    }
    public boolean canDoubleJump(){
        return !ground && doubleJump;
    }
    public void consumeDoubleJump(){
        doubleJump = false;
        isCurrentlyDoubleJumping = true;
    }
    public void onLand() {
        ground      = true;
        doubleJump  = true;
        isCurrentlyDoubleJumping = false;
        canDash        = true;
        pogoJump   = false;
        lastSafeX = position.x;
        lastSafeY = position.y;
    }
    public void leaveGround(){
        ground = false;
    }
    public void onPogoSuccess(){
        doubleJump = true;
        canDash = true;
        pogoJump = true;
    }
    public void setTouchingWall(boolean left, boolean right) {
        touchingWallLeft  = left;
        touchingWallRight = right;
        wallSliding       = (left || right) && !ground && velocity.y < 0;
        if (wallSliding) {
            movementState = MovementState.WALL_SLIDING;
        }
    }


    public boolean canAttack()       { return attackCooldownTimer <= 0 && !hurt && alive; }

    public void startAttack(Direction dir) {
        attacking          = true;
        attackTimer        = 0f;
        attackDirection    = dir;
        float cooldown     = 0.4f / charmState.attackSpeedMultiplier;
        attackCooldownTimer = cooldown;
        movementState      = MovementState.ATTACKING;
        cancelFocus();

        if (dir == Direction.LEFT || dir == Direction.RIGHT) {
            useAltSlash = !useAltSlash;
        }

    }

    public void tickAttack(float delta) {
        if (attackCooldownTimer > 0)
            attackCooldownTimer = Math.max(0, attackCooldownTimer - delta);
        if (attacking) {
            attackTimer += delta;
            if (attackTimer >= 0.2f) {   // attack active window
                attacking   = false;
                attackTimer = 0f;
            }
        }
    }


    public void tickInvincibility(float delta) {
        if (!invincible) return;
        invincibilityTimer = Math.max(0, invincibilityTimer - delta);
        if (invincibilityTimer <= 0) invincible = false;
    }


    public void tickHurt(float delta) {
        if (!hurt) return;
        hurtTimer = Math.max(0, hurtTimer - delta);
        if (hurtTimer <= 0) {
            hurt = false;
            if (alive) movementState = MovementState.IDLE;
        }
    }


    public void respawn() {

        setPosition(new Vector2(lastSafeX, lastSafeY));
        velocity.set(0, 0);
        healthMasks    = maxHealthMasks;
        alive          = true;
        invincible     = false;
        hurt           = false;
        focus      = false;
        dashing        = false;
        canDash        = true;
        doubleJump  = false;
        movementState  = MovementState.IDLE;
    }

    public void rebuildCharmContext() {
        charmState.reset();
        for (Charm charm : charmSlots.getEquipped()) {
            charm.onEquip(charmState);
        }
        nailDamage = Math.round(BASE_NAIL_DAMAGE * charmState.nailDamageMultiplier);
    }

    public CharmSlots getCharmSlots() {
        return charmSlots;
    }

    public boolean equipCharm(Charm charm) {
        if (!charmSlots.equip(charm)) return false;
        rebuildCharmContext();
        return true;
    }

    public boolean unequipCharm(Charm charm) {
        if (!charmSlots.unequip(charm)) return false;
        rebuildCharmContext();
        return true;
    }


    public void activateGodMode()   { godMode = true; }
    public void deactivateGodMode() { godMode = false; }
    public void activateNoclip()    { noclip  = true;  ground = true; }
    public void deactivateNoclip()  { noclip  = false; }
    public void fillSoulAndHealth() {
        soulVessel.fillMax();
    }


    public void updateMovementState() {
        if (!alive)         { movementState = MovementState.DEAD;         return; }
        if (hurt)           { movementState = MovementState.HURT;         return; }
        if (dashing)        { movementState = MovementState.DASHING;      return; }
        if (focus)          { movementState = MovementState.FOCUSING;     return; }
        if (attacking)      { movementState = MovementState.ATTACKING;    return; }
        if (wallSliding)    { movementState = MovementState.WALL_SLIDING; return; }

        if (ground) {
            movementState = (Math.abs(velocity.x) > 1f)
                ? MovementState.RUNNING : MovementState.IDLE;
        } else {
            if (velocity.y > 20f) {
                movementState = MovementState.JUMPING;
            } else if (velocity.y < -50f) {
                movementState = MovementState.FALLING;
            } else {
                if (Math.abs(velocity.x) > 1f) {
                    movementState = MovementState.RUNNING;
                } else {
                    movementState = MovementState.IDLE;
                }
            }
        }
    }


    private void resetFlags() {
        invincible = false;
        invincibilityTimer = 0f;
        ground  = false;
        doubleJump = false;
        jumpHigher = false;
        canDash            = true;
        dashing            = false;
        dashTimer          = 0f;
        dashCooldownTimer  = 0f;
        wallSliding        = false;
        touchingWallLeft   = false;
        touchingWallRight  = false;
        attacking          = false;
        attackTimer        = 0f;
        attackCooldownTimer = 0f;
        focus           = false;
        focusTimer         = 0f;
        hurt               = false;
        hurtTimer          = 0f;
        pogoJump      = false;
    }

    public int getHealthMasks() {
        return healthMasks;
    }

    public int getMaxHealthMasks() {
        return maxHealthMasks;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public float getInvincibilityTimer() {
        return invincibilityTimer;
    }

    public boolean isGround() {
        return ground;
    }

    public void setGround(boolean ground) {
        this.ground = ground;
    }

    public boolean isDoubleJump() {
        return doubleJump;
    }

    public boolean isJumpHigher() {
        return jumpHigher;
    }

    public boolean isPogoJump() {
        return pogoJump;
    }

    public boolean isWallSliding() {
        return wallSliding;
    }

    public boolean isTouchingWallLeft() {
        return touchingWallLeft;
    }

    public boolean isTouchingWallRight() {
        return touchingWallRight;
    }

    public boolean isFocus() {
        return focus;
    }

    public float getFocusTimer() {
        return focusTimer;
    }

    public boolean isCanDash() {
        return canDash;
    }

    public boolean isDashing() {
        return dashing;
    }

    public float getDashTimer() {
        return dashTimer;
    }

    public float getDashCooldownTimer() {
        return dashCooldownTimer;
    }

    public Direction getDashDirection() {
        return dashDirection;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public float getAttackTimer() {
        return attackTimer;
    }

    public float getAttackCooldownTimer() {
        return attackCooldownTimer;
    }

    public Direction getAttackDirection() {
        return attackDirection;
    }

    public boolean isHurt() {
        return hurt;
    }

    public float getHurtTimer() {
        return hurtTimer;
    }

    public CharmState getCharmState() {
        return charmState;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public float getLastSafeX() {
        return lastSafeX;
    }

    public float getLastSafeY() {
        return lastSafeY;
    }

    public MovementState getMovementState() {
        return movementState;
    }

    public void setMovementState(MovementState movementState) {
        this.movementState = movementState;
    }

    public boolean isGodMode() {
        return godMode;
    }

    public boolean isNoclip() {
        return noclip;
    }

    public boolean isDialogueLocked() {
        return dialogueLocked;
    }

    public void setDialogueLocked(boolean dialogueLocked) {
        this.dialogueLocked = dialogueLocked;
        if (dialogueLocked) {
            velocity.setZero();
        }
    }

    public int getNailDamage() {
        return nailDamage;
    }

    public SoulVessel getSoulVessel() {
        return soulVessel;
    }

    public boolean isCurrentlyDoubleJumping() {
        return isCurrentlyDoubleJumping;
    }

    public boolean isUseAltSlash() {
        return useAltSlash;
    }

    public void setLastSafeY(float lastSafeY) {
        this.lastSafeY = lastSafeY;
    }

    public void setLastSafeX(float lastSafeX) {
        this.lastSafeX = lastSafeX;
    }

    public void increaseHealthMask() {
        this.healthMasks += 1;
    }

    public void setHealthMasks(int healthMasks) {
        this.healthMasks = Math.max(0, Math.min(healthMasks, maxHealthMasks));
    }

    public void setMaxHealthMasks(int maxHealthMasks) {
        this.maxHealthMasks = Math.max(1, maxHealthMasks);
        this.healthMasks = Math.min(healthMasks, this.maxHealthMasks);
    }

    /**
     * Restores knight progress from a loaded save without touching runtime-only objects.
     */
    public void restoreFromSave(
        float x,
        float y,
        float safeX,
        float safeY,
        int masks,
        int maxMasks,
        int souls
    ) {
        setMaxHealthMasks(maxMasks);
        setHealthMasks(masks);
        setPosition(new Vector2(x, y));
        lastSafeX = safeX;
        lastSafeY = safeY;
        soulVessel.setCurrentSouls(souls);
        alive = masks > 0;
        resetFlags();
        movementState = alive ? MovementState.IDLE : MovementState.DEAD;
    }
}
