package io.github.HollowKnight.Model.particle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class GreenpathLeafEffect implements ParticleEffect {

    private static final String TEXTURE_PATH =
        "HollowKnight/Particles & Effects/falling_leaf_particles.png";

    private static final int FRAME_COUNT = 3;
    private static final int FRAME_SIZE = 32;

    private static final int POOL_SIZE = 160;
    private static final float SPAWN_RATE = 6f;

    private static final float MIN_FALL_SPEED = 20f;
    private static final float MAX_FALL_SPEED = 50f;
    private static final float DRIFT_AMPLITUDE = 15f;
    private static final float DRIFT_FREQUENCY = 1.1f;
    private static final float MIN_ROTATION_SPEED = -40f;
    private static final float MAX_ROTATION_SPEED = 40f;
    private static final float MIN_SCALE = 0.7f;
    private static final float MAX_SCALE = 1.3f;
    private static final float SPAWN_MARGIN = 48f;
    private static final float DESPAWN_MARGIN = 32f;

    private static Texture leafTexture;
    private static TextureRegion[] frames;

    private static void ensureTextureLoaded() {
        if (leafTexture != null) {
            return;
        }
        leafTexture = new Texture(Gdx.files.internal(TEXTURE_PATH));
        leafTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = new TextureRegion(leafTexture, 0, i * FRAME_SIZE, FRAME_SIZE, FRAME_SIZE);
        }
    }

    public GreenpathLeafEffect() {
        ensureTextureLoaded();
    }

    @Override
    public int poolSize() {
        return POOL_SIZE;
    }

    @Override
    public int targetActiveCount() {
        return 0;
    }

    @Override
    public float spawnRatePerSecond() {
        return SPAWN_RATE;
    }

    @Override
    public boolean usesTexture() {
        return true;
    }

    @Override
    public TextureRegion getFrame(Particle particle) {
        ensureTextureLoaded();
        int index = particle.frameIndex;
        if (index < 0 || index >= frames.length) {
            index = 0;
        }
        return frames[index];
    }

    @Override
    public float getRotation(Particle particle) {
        return particle.rotation;
    }

    @Override
    public void initParticle(Particle particle, Rectangle cameraBounds) {
        float top = cameraBounds.y + cameraBounds.height;
        particle.anchorX = MathUtils.random(
            cameraBounds.x - SPAWN_MARGIN,
            cameraBounds.x + cameraBounds.width + SPAWN_MARGIN
        );
        particle.x = particle.anchorX;
        particle.y = top + MathUtils.random(0f, cameraBounds.height * 0.35f);

        particle.vy = -MathUtils.random(MIN_FALL_SPEED, MAX_FALL_SPEED);
        particle.size = FRAME_SIZE * MathUtils.random(MIN_SCALE, MAX_SCALE);
        particle.phase = MathUtils.random(0f, MathUtils.PI2);
        particle.rotation = MathUtils.random(0f, 360f);
        particle.rotationSpeed = MathUtils.random(MIN_ROTATION_SPEED, MAX_ROTATION_SPEED);
        particle.frameIndex = MathUtils.random(0, FRAME_COUNT - 1);
        particle.maxAlpha = MathUtils.random(0.75f, 1f);
        particle.age = 0f;
        particle.lifetime = Float.MAX_VALUE;
        particle.active = true;
    }

    @Override
    public void updateParticle(Particle particle, float delta) {
    }

    @Override
    public void updateParticle(Particle particle, float delta, Rectangle cameraBounds) {
        particle.age += delta;

        particle.y += particle.vy * delta;
        particle.rotation += particle.rotationSpeed * delta;

        float drift = MathUtils.sin(particle.age * DRIFT_FREQUENCY + particle.phase) * DRIFT_AMPLITUDE;
        particle.x = particle.anchorX + drift;

        float bottom = cameraBounds.y - DESPAWN_MARGIN;
        if (particle.y < bottom) {
            particle.deactivate();
        }
    }

    @Override
    public float colorR() {
        return 1f;
    }

    @Override
    public float colorG() {
        return 1f;
    }

    @Override
    public float colorB() {
        return 1f;
    }

    @Override
    public float currentAlpha(Particle particle) {
        return particle.maxAlpha;
    }
}
