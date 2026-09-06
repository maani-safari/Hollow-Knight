package io.github.HollowKnight.Model.particle;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public interface ParticleEffect {

    int poolSize();

    int targetActiveCount();
    void initParticle(Particle particle, Rectangle cameraBounds);

    void updateParticle(Particle particle, float delta);

    default void updateParticle(Particle particle, float delta, Rectangle cameraBounds) {
        updateParticle(particle, delta);
    }

    /** Values greater than zero enable continuous rate-based spawning instead of filling to target count. */
    default float spawnRatePerSecond() {
        return -1f;
    }

    default boolean usesTexture() {
        return false;
    }

    default TextureRegion getFrame(Particle particle) {
        return null;
    }

    default float getRotation(Particle particle) {
        return 0f;
    }

    float colorR();

    float colorG();

    float colorB();
    float currentAlpha(Particle particle);
}
