package io.github.HollowKnight.Controller;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.HollowKnight.Model.World.Room;
import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.entity.enemy.FalseKnight;
import io.github.HollowKnight.Model.entity.enemy.FalseKnightState;
import io.github.HollowKnight.Model.entity.enemy.FalseKnightAttack;
import io.github.HollowKnight.View.GameScreen;

import java.util.Random;

public class FalseKnightController {
    private final GameEventBus eventBus;
    private final Random random = new Random();

    private FalseKnight boss;
    private boolean fightStarted;
    private boolean bossSpawned;
    private boolean arenaLocked;
    private float stunTimer;
    private boolean stunEventPosted;
    private boolean defeatHandled;
    private GameScreen gameScreen;

    public FalseKnightController(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void update(float delta, Room room, Knight knight) {
        if (gameScreen != null && gameScreen.isFalseKnightDefeated()) {
            return;
        }

        if (!fightStarted) {
            checkBossTrigger(room, knight);
        }

        if (boss == null) {
            return;
        }

        if (!boss.isAlive() || boss.getAiState() == FalseKnightState.DEAD) {
            if (boss.isReadyToBeRemoved() && !defeatHandled) {
                onBossDefeated(room);
            }
            return;
        }

        trackGrounded(boss, room);
        updateStun(delta);
        updateAi(delta, knight);
        checkBossDamage(knight);
        checkShockwaveDamage(knight);
        checkBossHeavyImpact();
    }

    private void checkBossHeavyImpact() {
        if (boss != null && boss.consumeHeavyImpact()) {
            eventBus.post(GameEvent.BOSS_HEAVY_IMPACT);
        }
    }

    private void checkBossTrigger(Room room, Knight knight) {
        Rectangle trigger = room.getBossTriggerZone();
        Vector2 spawn = room.getBossSpawnPoint();
        if (trigger == null || spawn == null || !knight.isAlive()) {
            return;
        }
        if (!knight.getHitbox().overlaps(trigger)) {
            return;
        }

        fightStarted = true;
        eventBus.post(GameEvent.BOSS_FIGHT_STARTED);

        if (!bossSpawned) {
            spawnBoss(room, spawn);
        }
        lockArena(room);
    }

    private void spawnBoss(Room room, Vector2 spawn) {
        boss = new FalseKnight(spawn.x, spawn.y);
        boss.setFacingRight(false);
        room.addEnemy(boss);
        bossSpawned = true;
        eventBus.post(GameEvent.BOSS_SPAWNED);
    }

    private void lockArena(Room room) {
        if (arenaLocked) {
            return;
        }
        Rectangle enter = room.getEnterDoorRect();
        Rectangle exit = room.getExitDoorRect();
        if (enter != null) {
            room.addArenaWall(enter);
        }
        if (exit != null) {
            room.addArenaWall(exit);
        }
        arenaLocked = true;
    }

    private void unlockArena(Room room) {
        room.clearArenaWalls();
        arenaLocked = false;
    }

    private void onBossDefeated(Room room) {
        defeatHandled = true;
        unlockArena(room);
        if (gameScreen != null) {
            gameScreen.markFalseKnightDefeated();
        }
        removeBossFromRoom(room);
        boss = null;
    }

    private void removeBossFromRoom(Room room) {
        if (boss == null) {
            return;
        }
        for (int i = room.getEnemies().size - 1; i >= 0; i--) {
            if (room.getEnemies().get(i) == boss) {
                room.removeEnemyAt(i);
                eventBus.post(GameEvent.ENEMY_DIED);
                return;
            }
        }
    }

    private void updateStun(float delta) {
        if (boss == null || boss.getAiState() != FalseKnightState.STUNNED) {
            return;
        }
        if (!stunEventPosted) {
            stunEventPosted = true;
            eventBus.post(GameEvent.BOSS_STUNNED );
        }
        stunTimer += delta;
        if (stunTimer >= FalseKnight.STUN_DURATION) {
            stunTimer = 0f;
            boss.endStun();
            boss.enterPhase2();
            eventBus.post(GameEvent.BOSS_PHASE2);
        }
    }

    private void updateAi(float delta, Knight knight) {
        if (boss == null || !knight.isAlive()) {
            return;
        }

        if (boss.getAiState() == FalseKnightState.STUNNED) {
            if (!boss.isStunTriggered()) {
                return;
            }
            return;
        }

        if (!boss.canDecide(delta)) {
            return;
        }

        float distance = knight.getPosition().dst(boss.getPosition());
        FalseKnightAttack choice = chooseAttack(distance, boss.getLastAttack());

        boss.resetDecisionTimer();
        faceKnight(knight);

        if (distance > FalseKnight.FAR_RANGE) {
            if (choice == FalseKnightAttack.RUNNING_CHARGE) {
                float dir = knight.getPosition().x >= boss.getPosition().x ? 1f : -1f;
                boss.prepareRunningCharge(dir);
                return;
            }
            boss.prepareWalk();
            return;
        }

        if (distance > FalseKnight.CLOSE_RANGE) {
            switch (choice) {
                case RUNNING_CHARGE -> {
                    float dir = knight.getPosition().x >= boss.getPosition().x ? 1f : -1f;
                    boss.prepareRunningCharge(dir);
                }
                case OFFENSIVE_LEAP -> boss.prepareOffensiveLeap(knight.getPosition().cpy());
                case SHOCKWAVE_LANDING -> boss.prepareShockwaveLanding();
                default -> boss.prepareWalk();
            }
            return;
        }

        switch (choice) {
            case MACE_SLAM -> boss.prepareMaceSlam();
            case OFFENSIVE_LEAP -> boss.prepareOffensiveLeap(knight.getPosition().cpy());
            case SHOCKWAVE_LANDING -> boss.prepareShockwaveLanding();
            case RUNNING_CHARGE -> {
                float dir = knight.getPosition().x >= boss.getPosition().x ? 1f : -1f;
                boss.prepareRunningCharge(dir);
            }
            default -> boss.prepareIdle();
        }
    }

    private FalseKnightAttack chooseAttack(float distance, FalseKnightAttack lastAttack) {
        float roll = random.nextFloat();
        FalseKnightAttack choice;

        if (distance <= FalseKnight.CLOSE_RANGE) {
            if (roll < 0.55f) choice = FalseKnightAttack.MACE_SLAM;
            else if (roll < 0.8f) choice = FalseKnightAttack.OFFENSIVE_LEAP;
            else choice = FalseKnightAttack.RUNNING_CHARGE;
        } else if (distance <= FalseKnight.FAR_RANGE) {
            if (roll < 0.35f) choice = FalseKnightAttack.MACE_SLAM;
            else if (roll < 0.65f) choice = FalseKnightAttack.OFFENSIVE_LEAP;
            else choice = FalseKnightAttack.RUNNING_CHARGE;
        } else {
            if (roll < 0.7f) choice = FalseKnightAttack.RUNNING_CHARGE;
            else if (roll < 0.9f) choice = FalseKnightAttack.OFFENSIVE_LEAP;
            else choice = FalseKnightAttack.MACE_SLAM;
        }

        if (boss != null && boss.isPhase2() && roll > 0.45f && distance < FalseKnight.FAR_RANGE) {
            choice = FalseKnightAttack.SHOCKWAVE_LANDING;
        }

        if (choice == lastAttack) {
            choice = pickAlternate(choice, distance);
        }
        return choice;
    }

    private FalseKnightAttack pickAlternate(FalseKnightAttack current, float distance) {
        if (distance <= FalseKnight.CLOSE_RANGE) {
            return current == FalseKnightAttack.MACE_SLAM
                ? FalseKnightAttack.OFFENSIVE_LEAP
                : FalseKnightAttack.MACE_SLAM;
        }
        if (distance <= FalseKnight.FAR_RANGE) {
            return current == FalseKnightAttack.OFFENSIVE_LEAP
                ? FalseKnightAttack.RUNNING_CHARGE
                : FalseKnightAttack.OFFENSIVE_LEAP;
        }
        return current == FalseKnightAttack.RUNNING_CHARGE
            ? FalseKnightAttack.OFFENSIVE_LEAP
            : FalseKnightAttack.RUNNING_CHARGE;
    }

    private void faceKnight(Knight knight) {
        boss.setFacingRight(knight.getPosition().x >= boss.getPosition().x);
    }

    private void checkBossDamage(Knight knight) {
        if (boss == null || !knight.isAlive() || knight.isInvincible()) {
            return;
        }
        Rectangle attackBox = boss.getAttackHitbox();
        if (attackBox.width <= 0f || attackBox.height <= 0f) {
            return;
        }
        if (!attackBox.overlaps(knight.getHitbox())) {
            return;
        }
        if (knight.applyKnockBack(boss.getContactDamage(), boss.getPosition().x)) {
            eventBus.post(GameEvent.KNIGHT_TOOK_DAMAGE);
            boss.onChargeBlocked();
        }
    }

    private void checkShockwaveDamage(Knight knight) {
        if (boss == null || !knight.isAlive() || knight.isInvincible()) {
            return;
        }
        int shockDamage = boss.getContactDamage();
        for (FalseKnight.Shockwave wave : boss.getShockwaves()) {
            if (!wave.active || !wave.hitbox.overlaps(knight.getHitbox())) {
                continue;
            }
            if (knight.applyKnockBack(shockDamage, wave.x)) {
                eventBus.post(GameEvent.KNIGHT_TOOK_DAMAGE);
                wave.active = false;
            }
        }
    }

    private void trackGrounded(FalseKnight enemy, Room room) {
        Rectangle box = enemy.getHitbox();
        boolean grounded = false;
        for (Rectangle platform : room.getPlatforms()) {
            if (Math.abs(box.y - (platform.y + platform.height)) < 4f
                && box.x + box.width > platform.x
                && box.x < platform.x + platform.width) {
                grounded = true;
                break;
            }
        }
        for (Rectangle wall : room.getArenaWalls()) {
            if (Math.abs(box.y - (wall.y + wall.height)) < 4f
                && box.x + box.width > wall.x
                && box.x < wall.x + wall.width) {
                grounded = true;
                break;
            }
        }
        enemy.setGrounded(grounded);
    }

    public void onBossHitStun() {
        if (boss != null && boss.getAiState() == FalseKnightState.STUNNED && !stunEventPosted) {
            stunEventPosted = true;
            eventBus.post(GameEvent.BOSS_STUNNED);
        }
    }

    public FalseKnight getBoss() {
        return boss;
    }

    public boolean isFightStarted() {
        return fightStarted;
    }

    public boolean isArenaLocked() {
        return arenaLocked;
    }
}
