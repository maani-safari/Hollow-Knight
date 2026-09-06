package io.github.HollowKnight.Model.particle;

import com.badlogic.gdx.math.Rectangle;


public class EnvironmentalParticleSystem {

    private final ParticleEffect effect;
    private final Particle[] pool;
    private final Rectangle cameraBounds = new Rectangle();
    private int activeCount;
    private float spawnAccumulator;

    public EnvironmentalParticleSystem(ParticleEffect effect) {
        this.effect = effect;
        this.pool = new Particle[effect.poolSize()];
        for (int i = 0; i < pool.length; i++) {
            pool[i] = new Particle();
        }
    }

    public ParticleEffect getEffect() {
        return effect;
    }

    public Particle[] getPool() {
        return pool;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void update(float delta, float camX, float camY, float camWidth, float camHeight) {
        cameraBounds.set(camX - camWidth * 0.5f, camY - camHeight * 0.5f, camWidth, camHeight);

        activeCount = 0;
        for (Particle particle : pool) {
            if (!particle.active) {
                continue;
            }
            effect.updateParticle(particle, delta, cameraBounds);
            if (particle.active) {
                activeCount++;
            }
        }

        float spawnRate = effect.spawnRatePerSecond();
        if (spawnRate > 0f) {
            spawnAccumulator += delta * spawnRate;
            int toSpawn = (int) spawnAccumulator;
            spawnAccumulator -= toSpawn;
            spawnInactiveParticles(toSpawn);
            return;
        }

        int toSpawn = effect.targetActiveCount() - activeCount;
        if (toSpawn <= 0) {
            return;
        }

        spawnInactiveParticles(toSpawn);
    }

    private void spawnInactiveParticles(int toSpawn) {
        for (Particle particle : pool) {
            if (!particle.active) {
                effect.initParticle(particle, cameraBounds);
                activeCount++;
                if (--toSpawn <= 0) {
                    break;
                }
            }
        }
    }
}
