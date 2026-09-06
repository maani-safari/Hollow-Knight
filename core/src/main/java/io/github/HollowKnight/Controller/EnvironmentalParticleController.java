package io.github.HollowKnight.Controller;

import com.badlogic.gdx.graphics.OrthographicCamera;
import io.github.HollowKnight.Model.particle.EnvironmentalParticleSystem;
import io.github.HollowKnight.Model.particle.ParticleEffect;
import io.github.HollowKnight.Model.particle.ParticleEffectRegistry;

public class EnvironmentalParticleController {

    private final EnvironmentalParticleSystem system;
    private boolean enabled;

    public EnvironmentalParticleController(String effectId) {
        ParticleEffect effect = ParticleEffectRegistry.get(effectId);
        this.system = new EnvironmentalParticleSystem(effect);
        this.enabled = true;
    }

    public EnvironmentalParticleController(EnvironmentalParticleSystem system) {
        this.system = system;
        this.enabled = true;
    }

    public EnvironmentalParticleSystem getSystem() {
        return system;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void update(float delta, OrthographicCamera camera) {
        if (!enabled || camera == null) {
            return;
        }
        system.update(
            delta,
            camera.position.x,
            camera.position.y,
            camera.viewportWidth,
            camera.viewportHeight
        );
    }
}
