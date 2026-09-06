package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.HollowKnight.Model.entity.Direction;
import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.entity.MovementState;

public class KnightView {
    private final Knight knight;
    private float stateTime;
    private TextureAtlas atlas;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> fallAnimation;
    private Animation<TextureRegion> dashAnimation;
    private Animation<TextureRegion> hurtAnimation;
    private Animation<TextureRegion> wallSlideAnimation;
    private Animation<TextureRegion> wallJumpAnimation;
    private Animation<TextureRegion> focusStartAnimation;
    private Animation<TextureRegion> focusGetAnimation;
    private Animation<TextureRegion> focusEndAnimation;
    private Animation<TextureRegion> slashAnimation;
    private Animation<TextureRegion> doubleJumpAnimation;
    private Animation<TextureRegion> altSlashAnimation;
    private Animation<TextureRegion> downSlashAnimation;
    private Animation<TextureRegion> upSlashAnimation;
    private Animation<TextureRegion> deathAnimation;
    private Animation<TextureRegion> upSlashEffect;
    private Animation<TextureRegion> downSlashEffect;
    private Animation<TextureRegion> slashEffect;
    private Animation<TextureRegion> altSlashEffect;


    private float doubleJumpTime = 0f;
    private boolean wasDoubleJumping = false;

    public KnightView(Knight knight) {
        this.knight = knight;
        this.stateTime = 0f;

        initAnimations();
    }

    private void initAnimations() {
        atlas = new TextureAtlas(Gdx.files.internal("atlas/Knight.atlas"));
        idleAnimation = new Animation<>(0.1f, atlas.findRegions("Idle"), Animation.PlayMode.LOOP);
        runAnimation = new Animation<>(0.07f, atlas.findRegions("Run"), Animation.PlayMode.LOOP);
        jumpAnimation = new Animation<>(0.1f, atlas.findRegions("Airborne"), Animation.PlayMode.NORMAL);
        doubleJumpAnimation = new Animation<>(0.1f , atlas.findRegions("Double Jump"), Animation.PlayMode.NORMAL);
        fallAnimation = new Animation<>(0.1f, atlas.findRegions("Fall"), Animation.PlayMode.LOOP);
        dashAnimation = new Animation<>(0.05f, atlas.findRegions("Dash"), Animation.PlayMode.NORMAL);
        hurtAnimation = new Animation<>(0.1f, atlas.findRegions("Idle Hurt"), Animation.PlayMode.LOOP);
        wallSlideAnimation = new Animation<>(0.1f, atlas.findRegions("Wall Slide"), Animation.PlayMode.LOOP);
        wallJumpAnimation = new Animation<>(0.1f, atlas.findRegions("Walljump"), Animation.PlayMode.NORMAL);
        focusStartAnimation = new Animation<>(0.1f, atlas.findRegions("Focus Start"), Animation.PlayMode.NORMAL);
        focusGetAnimation = new Animation<>(0.1f, atlas.findRegions("Focus Get"), Animation.PlayMode.NORMAL);
        focusEndAnimation = new Animation<>(0.1f, atlas.findRegions("Focus End"), Animation.PlayMode.NORMAL);
        slashAnimation = new Animation<>(0.1f, atlas.findRegions("Slash"), Animation.PlayMode.NORMAL);
        altSlashAnimation = new Animation<>(0.1f, atlas.findRegions("SlashAlt"), Animation.PlayMode.NORMAL);
        upSlashAnimation = new Animation<>(0.1f, atlas.findRegions("UpSlash"), Animation.PlayMode.NORMAL);
        downSlashAnimation = new Animation<>(0.1f, atlas.findRegions("DownSlash"), Animation.PlayMode.NORMAL);
        deathAnimation = new Animation<>(0.1f, atlas.findRegions("Death"), Animation.PlayMode.NORMAL);


        upSlashEffect = new Animation<>(0.05f , atlas.findRegions("UpSlashEffect"),Animation.PlayMode.NORMAL);
        downSlashEffect = new Animation<>(0.05f , atlas.findRegions("DownSlashEffect"),Animation.PlayMode.NORMAL);
        slashEffect = new Animation<>(0.05f , atlas.findRegions("SlashEffect"),Animation.PlayMode.NORMAL);
        altSlashEffect = new Animation<>(0.05f , atlas.findRegions("SlashEffectAlt"),Animation.PlayMode.NORMAL);




    }
    private MovementState lastState = MovementState.IDLE;
    public void update(float delta) {
        if (knight.getMovementState() != lastState) {
            stateTime = 0;
            lastState = knight.getMovementState();
        }
        stateTime += delta;
        if (knight.isDoubleJump()) {
            doubleJumpTime += delta;
        } else {
            doubleJumpTime = 0f;
        }
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = getCurrentFrame();
        if (knight.isInvincible() && shouldBlinkOff()) {
            return;
        }
        if (!knight.isFacingRight() && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (knight.isFacingRight() && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        float offsetX = -140f;
        float offsetY = 0f;
        batch.draw(currentFrame, knight.getPosition().x + offsetX, knight.getPosition().y);
    }
    private boolean shouldBlinkOff() {
        return ((int) (knight.getInvincibilityTimer() * 10f)) % 2 == 0;
    }
    public void drawSlashEffect(SpriteBatch batch) {
        if (!knight.isAttacking()) return;

        float attackTime = knight.getAttackTimer();
        TextureRegion effectFrame = null;

        float x = knight.getPosition().x;
        float y = knight.getPosition().y;
        float w = knight.getHitbox().width;
        float h = knight.getHitbox().height;

        float baseWidth = 150f;
        float baseHeight = 80f;
        float scale = 2.5f;

        float effectWidth = baseWidth * scale;
        float effectHeight = baseHeight * scale;

        switch (knight.getAttackDirection()) {
            case RIGHT:
                effectFrame = knight.isUseAltSlash() ? altSlashEffect.getKeyFrame(attackTime, false) : slashEffect.getKeyFrame(attackTime, false);
                if (!effectFrame.isFlipX()) {
                    effectFrame.flip(true, false);
                }
                batch.draw(effectFrame, x + w - 200, y - 70, effectWidth, effectHeight);
                break;

            case LEFT:
                effectFrame = knight.isUseAltSlash() ? altSlashEffect.getKeyFrame(attackTime, false) : slashEffect.getKeyFrame(attackTime, false);
                if (effectFrame.isFlipX()) effectFrame.flip(true, false);
                batch.draw(effectFrame, x - effectWidth + 200, y -70, effectWidth, effectHeight);
                break;

            case UP:
                effectFrame = upSlashEffect.getKeyFrame(attackTime, false);
                batch.draw(effectFrame, x - 80, y + h - 50, effectHeight, effectWidth);
                break;

            case DOWN:
                effectFrame = downSlashEffect.getKeyFrame(attackTime, false);
                batch.draw(effectFrame, x - 80, y - effectWidth + 50, effectHeight, effectWidth);
                break;
        }
    }
    private TextureRegion getCurrentFrame() {
        MovementState state = knight.getMovementState();
        switch (state) {
            case RUNNING:
                return runAnimation.getKeyFrame(stateTime, true);
            case JUMPING:
                if (knight.isCurrentlyDoubleJumping())
                    return doubleJumpAnimation.getKeyFrame(doubleJumpTime,false);
                else
                    return jumpAnimation.getKeyFrame(stateTime, false);
            case FALLING:
                if (knight.isCurrentlyDoubleJumping())
                    return doubleJumpAnimation.getKeyFrame(stateTime,false);
                return fallAnimation.getKeyFrame(stateTime, false);
            case DASHING:
                return dashAnimation.getKeyFrame(stateTime, false);
            case HURT:
                return hurtAnimation.getKeyFrame(stateTime, true);
            case IDLE:
                return idleAnimation.getKeyFrame(stateTime, true);
            case WALL_SLIDING:
                return wallSlideAnimation.getKeyFrame(stateTime, true);
            case WALL_JUMPING:
                return wallJumpAnimation.getKeyFrame(stateTime, false);
            case FOCUSING:
                float focusTime = knight.getFocusTimer();
                if (focusTime < focusStartAnimation.getAnimationDuration()) {
                    return focusStartAnimation.getKeyFrame(focusTime, false);
                }
                else if (knight.isFocus()) {

                    float loopTime = focusTime - focusStartAnimation.getAnimationDuration();
                    return focusGetAnimation.getKeyFrame(loopTime, true);
                }
                else {
                    float endTime = focusTime - focusStartAnimation.getAnimationDuration();
                    return focusEndAnimation.getKeyFrame(endTime, false);
                }
            case ATTACKING:
                float attackTime = knight.getAttackTimer();
                if (knight.getAttackDirection() == Direction.UP) {
                    return upSlashAnimation.getKeyFrame(attackTime, false);
                } else if (knight.getAttackDirection() == Direction.DOWN) {
                    return downSlashAnimation.getKeyFrame(attackTime, false);
                } else {
                    if (knight.isUseAltSlash()) {
                        return altSlashAnimation.getKeyFrame(attackTime, false);
                    } else {
                        return slashAnimation.getKeyFrame(attackTime, false);
                    }
                }
            case DEAD:
                return deathAnimation.getKeyFrame(stateTime,false);
            default:
                return idleAnimation.getKeyFrame(stateTime, true);
        }
    }

    public void resetStateTime() {
        this.stateTime = 0f;
    }

    public void dispose() {
        if (atlas != null) {
            atlas.dispose();
        }
    }
    public boolean isDeathAnimationFinished() {
        return deathAnimation.isAnimationFinished(stateTime);
    }
}

