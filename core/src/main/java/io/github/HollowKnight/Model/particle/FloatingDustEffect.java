package io.github.HollowKnight.Model.particle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class FloatingDustEffect implements ParticleEffect {

    private static final int POOL_SIZE = 256;
    private static final int TARGET_ACTIVE = 200;

    private static final float MIN_SIZE = 4.5f;
    private static final float MAX_SIZE = 10.5f;
    private static final float MIN_LIFETIME = 4f;
    private static final float MAX_LIFETIME = 11f;
    private static final float MIN_SPEED = 4f;
    private static final float MAX_SPEED = 14f;
    private static final float MIN_ALPHA = 0.12f;
    private static final float MAX_ALPHA = 0.42f;

    private static final float FADE_IN_RATIO = 0.18f;
    private static final float FADE_OUT_RATIO = 0.28f;

    private static final float FLOAT_AMPLITUDE = 10f;
    private static final float FLOAT_FREQUENCY_MIN = 0.6f;
    private static final float FLOAT_FREQUENCY_MAX = 1.4f;

    @Override
    public int poolSize() {
        return POOL_SIZE;
    }

    @Override
    public int targetActiveCount() {
        return TARGET_ACTIVE;
    }

    @Override
    public void initParticle(Particle particle, Rectangle cameraBounds) {
        particle.x = MathUtils.random(cameraBounds.x, cameraBounds.x + cameraBounds.width);
        particle.y = MathUtils.random(cameraBounds.y, cameraBounds.y + cameraBounds.height);

        float angle = MathUtils.random(0f, MathUtils.PI2);
        float speed = MathUtils.random(MIN_SPEED, MAX_SPEED);
        particle.vx = MathUtils.cos(angle) * speed;
        particle.vy = MathUtils.sin(angle) * speed * 0.35f;

        particle.size = MathUtils.random(MIN_SIZE, MAX_SIZE);
        particle.lifetime = MathUtils.random(MIN_LIFETIME, MAX_LIFETIME);
        particle.age = 0f;
        particle.maxAlpha = MathUtils.random(MIN_ALPHA, MAX_ALPHA);
        particle.phase = MathUtils.random(0f, MathUtils.PI2);
        particle.active = true;
    }

    @Override
    public void updateParticle(Particle particle, float delta) {
        particle.age += delta;

        float floatFreq = FLOAT_FREQUENCY_MIN
            + (particle.phase / MathUtils.PI2) * (FLOAT_FREQUENCY_MAX - FLOAT_FREQUENCY_MIN);
        particle.y += MathUtils.sin(particle.age * floatFreq + particle.phase) * FLOAT_AMPLITUDE * delta;

        particle.x += particle.vx * delta;
        particle.y += particle.vy * delta;

        if (particle.age >= particle.lifetime) {
            particle.deactivate();
        }
    }

    @Override
    public float colorR() {
        return 0.88f;
    }

    @Override
    public float colorG() {
        return 0.84f;
    }

    @Override
    public float colorB() {
        return 0.76f;
    }

    @Override
    public float currentAlpha(Particle particle) {
        float t = particle.age / particle.lifetime;
        float alpha = particle.maxAlpha;

        if (t < FADE_IN_RATIO) {
            alpha *= t / FADE_IN_RATIO;
        } else if (t > 1f - FADE_OUT_RATIO) {
            alpha *= (1f - t) / FADE_OUT_RATIO;
        }

        return Math.max(0f, alpha);
    }
}
