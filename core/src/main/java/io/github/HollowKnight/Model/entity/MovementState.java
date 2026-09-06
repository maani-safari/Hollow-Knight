package io.github.HollowKnight.Model.entity;

public enum MovementState {
    IDLE,
    RUNNING,
    JUMPING,        // ascending
    FALLING,        // descending
    DASHING,
    WALL_SLIDING,
    WALL_JUMPING,
    ATTACKING,
    CASTING_SPELL,
    FOCUSING,       // heal channeling
    HURT,           // knockback frames
    DEAD
}
