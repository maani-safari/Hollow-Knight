package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import io.github.HollowKnight.Model.entity.enemy.FalseKnight;
import io.github.HollowKnight.Model.entity.enemy.FalseKnightState;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FalseKnightView {
    private static final float BOSS_SCALE_X = 3f;
    private static final float BOSS_SCALE_Y = 1.5f;
    private static final float DRAW_OFFSET_X = 320f;
    private static final float SHOCKWAVE_FRAME_DURATION = 0.06f;
    private static final int SHOCKWAVE_FRAME_COUNT = 7;

    private final TextureAtlas atlas;
    private final Map<String, Animation<TextureRegion>> animations = new HashMap<>();
    private final Array<Texture> shockwaveTextures = new Array<>();
    private Animation<TextureRegion> shockwaveAnimation;

    public FalseKnightView() {
        atlas = new TextureAtlas(Gdx.files.internal("atlas/FalseKnight.atlas"));
        loadAnimation("IDLE", "Idle", 0.12f, Animation.PlayMode.LOOP);
        loadAnimation("RUN", "Run", 0.08f, Animation.PlayMode.LOOP);
        loadAnimation("RUN_ANTIC", "Run Antic", 0.1f, Animation.PlayMode.NORMAL);
        loadAnimation("TURN", "Turn", 0.15f, Animation.PlayMode.NORMAL);
        loadAnimation("JUMP", "Jump", 0.1f, Animation.PlayMode.NORMAL);
        loadAnimation("LAND", "Land", 0.08f, Animation.PlayMode.NORMAL);
        loadAnimation("ATTACK_ANTIC", "Attack Antic", 0.09f, Animation.PlayMode.NORMAL);
        loadAnimation("ATTACK", "Attack", 0.1f, Animation.PlayMode.NORMAL);
        loadAnimation("ATTACK_RECOVER", "Attack Recover", 0.09f, Animation.PlayMode.NORMAL);
        loadAnimation("JUMP_ATTACK", "Jump Attack", 0.08f, Animation.PlayMode.NORMAL);
        loadStunAnimation();
        loadAnimation("STUN_RECOVER", "Stun Recover", 0.1f, Animation.PlayMode.NORMAL);
        loadAnimation("DEATH_FALL", "DeathFall", 0.12f, Animation.PlayMode.NORMAL);
        loadAnimation("DEATH_HIT", "DeathHit", 0.12f, Animation.PlayMode.NORMAL);
        loadAnimation("DEATH_LAND", "DeathLand", 0.1f, Animation.PlayMode.NORMAL);
        shockwaveAnimation = loadShockwaveAnimation();
    }

    private void loadAnimation(String key, String regionName, float frameDuration,
                               Animation.PlayMode playMode) {
        if (atlas.findRegions(regionName).size == 0) {
            return;
        }
        animations.put(key, new Animation<>(frameDuration, atlas.findRegions(regionName), playMode));
    }

    private void loadStunAnimation() {
        if (atlas.findRegions("Stun").size > 0) {
            loadAnimation("STUN", "Stun", 0.12f, Animation.PlayMode.LOOP);
            return;
        }
        loadAnimation("STUN", "Body", 0.12f, Animation.PlayMode.LOOP);
    }

    private Animation<TextureRegion> loadShockwaveAnimation() {
        Animation<TextureRegion> fromAssets = tryLoadShockwaveFromAssets();
        if (fromAssets != null) {
            return fromAssets;
        }
        return createProceduralShockwaveAnimation();
    }

    private Animation<TextureRegion> tryLoadShockwaveFromAssets() {
        Array<TextureRegion> frames = new Array<>(SHOCKWAVE_FRAME_COUNT);
        for (int i = 0; i < SHOCKWAVE_FRAME_COUNT; i++) {
            String spurtPath = String.format(Locale.US, "HollowKnight/animation/Projectile/Shockwave_%03d.png", i);

            if (spurtPath == null) {
                return null;
            }
            Texture texture = new Texture(Gdx.files.internal(spurtPath));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            shockwaveTextures.add(texture);
            frames.add(new TextureRegion(texture));
        }
        Animation<TextureRegion> animation = new Animation<>(SHOCKWAVE_FRAME_DURATION, frames,
            Animation.PlayMode.NORMAL);

        return animation;
    }

    private Animation<TextureRegion> createProceduralShockwaveAnimation() {
        Array<TextureRegion> frames = new Array<>(SHOCKWAVE_FRAME_COUNT);
        for (int i = 0; i < SHOCKWAVE_FRAME_COUNT; i++) {
            int width = 60 + i * 20;
            int height = 80;
            Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 0f);
            pixmap.fill();

            float alpha = 0.75f - i * 0.1f;
            int barW = Math.max(4, width - 6 - i * 2);
            int barH = Math.max(2, 8 - i);
            int barX = (width - barW) / 2;
            int barY = (height - barH) / 2;

            pixmap.setColor(0.88f, 0.84f, 0.78f, alpha);
            pixmap.fillRectangle(barX, barY, barW, barH);

            pixmap.setColor(0.95f, 0.92f, 0.88f, alpha * 0.55f);
            int coreH = Math.max(1, barH - 3);
            int coreW = Math.max(2, barW - 12);
            pixmap.fillRectangle((width - coreW) / 2, barY + (barH - coreH) / 2, coreW, coreH);

            Texture texture = new Texture(pixmap);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            pixmap.dispose();
            shockwaveTextures.add(texture);
            frames.add(new TextureRegion(texture));
        }
        return new Animation<>(SHOCKWAVE_FRAME_DURATION, frames, Animation.PlayMode.NORMAL);
    }

    public void render(SpriteBatch batch, FalseKnight boss, float delta) {
        if (boss == null) {
            return;
        }

        String animationKey = resolveArmorAnimationKey(boss);
        Animation<TextureRegion> armorAnimation;
        if (boss.getAiState() == FalseKnightState.STUNNED) {
            armorAnimation = animations.get("STUN");
        } else {
            armorAnimation = animations.getOrDefault(animationKey, animations.get("IDLE"));
        }
        if (armorAnimation == null) {
            return;
        }

        float animTime = boss.getStateTimer();
        if (boss.isPhase2() && isAttackState(boss.getAiState())) {
            animTime *= 1.3f;
        }
        boolean armorLooping = boss.getAiState() == FalseKnightState.STUNNED;
        TextureRegion armorFrame = armorAnimation.getKeyFrame(animTime, armorLooping);

        float drawX = boss.getPosition().x - DRAW_OFFSET_X;
        float drawY = boss.getPosition().y;
        float armorWidth = boss.getHitbox().width * BOSS_SCALE_X;
        float armorHeight = boss.getHitbox().height * BOSS_SCALE_Y;
        boolean flip = boss.isFacingRight();

        drawFlipped(batch, armorFrame, drawX, drawY, armorWidth, armorHeight, flip);
        drawShockwaves(batch, boss);
    }

    private boolean isAttackState(FalseKnightState state) {
        return state == FalseKnightState.RUNNING_CHARGE
            || state == FalseKnightState.MACE_SLAM
            || state == FalseKnightState.JUMP
            || state == FalseKnightState.SHOCKWAVE_LANDING
            || state == FalseKnightState.DEFENSIVE_LEAP;
    }

    private void drawShockwaves(SpriteBatch batch, FalseKnight boss) {
        if (shockwaveAnimation == null) {
            return;
        }
        for (FalseKnight.Shockwave wave : boss.getShockwaves()) {
            if (!wave.active) {
                continue;
            }
            float age = wave.maxLifetime - wave.lifetime;
            TextureRegion frame = shockwaveAnimation.getKeyFrame(age, false);
            float drawY = wave.y + wave.height * 0.15f;
            drawFlipped(batch, frame, wave.x, drawY, wave.width, wave.height, !wave.facingRight);
        }
    }

    private void drawFlipped(SpriteBatch batch, TextureRegion frame,
                             float x, float y, float width, float height, boolean flip) {
        if (flip) {
            batch.draw(frame, x + width, y, -width, height);
        } else {
            batch.draw(frame, x, y, width, height);
        }
    }

    private String resolveArmorAnimationKey(FalseKnight boss) {
        return switch (boss.getAiState()) {
            case IDLE -> "IDLE";
            case WALK -> "RUN";
            case RUNNING_CHARGE -> {
                if (boss.getChargePhase() == 0) yield "RUN_ANTIC";
                if (boss.getChargePhase() == 1) yield "RUN";
                yield "ATTACK_RECOVER";
            }
            case JUMP -> {
                if (boss.getLeapPhase() <= 1) yield "JUMP";
                yield "LAND";
            }
            case MACE_SLAM -> {
                if (boss.getMacePhase() == 0) yield "ATTACK_ANTIC";
                if (boss.getMacePhase() == 1) yield "ATTACK";
                yield "ATTACK_RECOVER";
            }
            case SHOCKWAVE_LANDING -> {
                if (boss.getShockPhase() == 0) yield "JUMP_ATTACK";
                yield "LAND";
            }
            case DEFENSIVE_LEAP -> "JUMP";
            case STUNNED -> "STUN";
            case DEAD -> {
                float t = boss.getStateTimer();
                if (t < 0.6f) yield "DEATH_FALL";
                if (t < 1.2f) yield "DEATH_HIT";
                yield "DEATH_LAND";
            }
        };
    }

    public void dispose() {
        for (Texture texture : shockwaveTextures) {
            texture.dispose();
        }
        shockwaveTextures.clear();
        if (atlas != null) {
            atlas.dispose();
        }
        animations.clear();
    }
}
