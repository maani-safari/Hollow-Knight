package io.github.HollowKnight.Model.particle;

public class Particle {

    float x;
    float y;
    float vx;
    float vy;
    float size;
    float lifetime;
    float age;
    float maxAlpha;
    float phase;
    float rotation;
    float rotationSpeed;
    float anchorX;
    int frameIndex;
    boolean active;

    public boolean isActive() {
        return active;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }

    void deactivate() {
        active = false;
        age = 0f;
    }
}
