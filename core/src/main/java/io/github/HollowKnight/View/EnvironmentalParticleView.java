package io.github.HollowKnight.View;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.HollowKnight.Controller.EnvironmentalParticleController;
import io.github.HollowKnight.Model.particle.Particle;
import io.github.HollowKnight.Model.particle.ParticleEffect;
import io.github.HollowKnight.Model.particle.EnvironmentalParticleSystem;


public class EnvironmentalParticleView {

    private final Texture pixel;

    public EnvironmentalParticleView() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
    }

    public void draw(SpriteBatch batch, EnvironmentalParticleController controller) {
        if (controller == null || !controller.isEnabled()) {
            return;
        }

        EnvironmentalParticleSystem system = controller.getSystem();
        ParticleEffect effect = system.getEffect();
        Particle[] pool = system.getPool();

        float r = effect.colorR();
        float g = effect.colorG();
        float b = effect.colorB();

        for (Particle particle : pool) {
            if (!particle.isActive()) {
                continue;
            }

            float alpha = effect.currentAlpha(particle);
            if (alpha <= 0f) {
                continue;
            }

            batch.setColor(r, g, b, alpha);

            if (effect.usesTexture()) {
                TextureRegion frame = effect.getFrame(particle);
                if (frame == null) {
                    continue;
                }
                float width = particle.getSize();
                float height = width;
                float originX = width * 0.5f;
                float originY = height * 0.5f;
                batch.draw(
                    frame,
                    particle.getX() - originX,
                    particle.getY() - originY,
                    originX,
                    originY,
                    width,
                    height,
                    1f,
                    1f,
                    effect.getRotation(particle)
                );
            } else {
                float half = particle.getSize() * 0.5f;
                batch.draw(pixel, particle.getX() - half, particle.getY() - half, particle.getSize(), particle.getSize());
            }
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void dispose() {
        if (pixel != null) {
            pixel.dispose();
        }
    }
}
