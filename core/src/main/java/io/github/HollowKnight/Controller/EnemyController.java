package io.github.HollowKnight.Controller;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.HollowKnight.Model.World.Room;
import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.entity.enemy.*;

public class EnemyController {
    public static final float GRAVITY = -800f;
    public static final float MAX_FALL_SPEED = -600f;
    private final GameEventBus eventBus;

    public EnemyController(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void update(float delta, Room room, Knight knight) {
        EnemyContext ctx = new EnemyContext(
            knight.getPosition().cpy(), knight.isAlive(), room
        );

        Array<Enemy> enemies = room.getEnemies();
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.tickState(delta);
            if (enemy.getEnemyState() == EnemyState.DEATH) {

                if (enemy.isReadyToBeRemoved() && !(enemy instanceof FalseKnight)) {
                    handleDeathCleanup(enemy, room, i);
                }

                continue;
            }

            applyGravity(enemy, delta);
            enemy.update(delta, ctx);
            resolveCollisions(enemy, room, delta);
            checkContactDamage(enemy, knight);
        }
    }

    private void applyGravity(Enemy enemy, float delta) {
        if (enemy instanceof FlyingEnemy) return;
        enemy.getVelocity().y = Math.max(MAX_FALL_SPEED,
            enemy.getVelocity().y + GRAVITY * delta);
    }

    private void resolveCollisions(Enemy enemy, Room room, float delta) {
        Vector2 pos = enemy.getPosition();
        pos.x += enemy.getVelocity().x * delta;
        pos.y += enemy.getVelocity().y * delta;
        enemy.syncHitbox();
        Rectangle box = enemy.getHitbox();
        boolean grounded = false;
        for (Rectangle platform : room.getPlatforms()) {
            if (!box.overlaps(platform)) continue;
            if (enemy.getVelocity().y < 0 && box.y < platform.y + platform.height) {
                pos.y = platform.y + platform.height;
                enemy.getVelocity().y = 0;
                grounded = true;
            } else if (enemy.getVelocity().x != 0) {
                if (enemy.getVelocity().x > 0) {
                    pos.x = platform.x - box.width;
                } else {
                    pos.x = platform.x + platform.width;
                }
                enemy.getVelocity().x = 0;
                notifyWallHit(enemy);
            }
            enemy.syncHitbox();
            box = enemy.getHitbox();
        }

        for (Rectangle arenaWall : room.getArenaWalls()) {
            if (!box.overlaps(arenaWall)) continue;
            if (enemy.getVelocity().y < 0 && box.y < arenaWall.y + arenaWall.height) {
                pos.y = arenaWall.y + arenaWall.height;
                enemy.getVelocity().y = 0;
                grounded = true;
            } else if (enemy.getVelocity().x != 0) {
                if (enemy.getVelocity().x > 0) {
                    pos.x = arenaWall.x - box.width;
                } else {
                    pos.x = arenaWall.x + arenaWall.width;
                }
                enemy.getVelocity().x = 0;
                notifyWallHit(enemy);
            }
            enemy.syncHitbox();
            box = enemy.getHitbox();
        }

        if (grounded && enemy instanceof GroundEnemy) {
            checkCliffAhead(enemy, room);
        }

    }
    private void checkCliffAhead(Enemy enemy , Room room){
        Rectangle box = enemy.getHitbox();
        float extraReach = (enemy instanceof HornHeadEnemy he ) ? he.getCliffProbe() : 0f;
        float probeX = enemy.isFacingRight() ? box.x + box.width + 2 + extraReach :
            box.x - 2 - extraReach;
        Rectangle probe = new Rectangle(probeX, box.y - 4, 1, 8);

        boolean groundAhead = false;
        for (Rectangle platform : room.getPlatforms()) {
            if (probe.overlaps(platform)) { groundAhead = true; break; }
        }

        if (!groundAhead) {
            notifyCliffDetected(enemy);
        }
    }
    private void notifyWallHit(Enemy enemy) {
        if (enemy instanceof FalseKnight fk) fk.onChargeBlocked();
        if (enemy instanceof CrystallizedEnemy crystallized) crystallized.onHitWall();
        if (enemy instanceof GroundEnemy ge) ge.onHitWall();
        if (enemy instanceof ChasingEnemy chasing) chasing.hitWallOrCliff();
    }

    private void notifyCliffDetected(Enemy enemy) {
        if (enemy instanceof GroundEnemy ge)     ge.onCLif();
        if (enemy instanceof ChasingEnemy ce)ce.hitWallOrCliff();
    }
    private void checkContactDamage(Enemy enemy, Knight knight) {
        if (!knight.isAlive() || knight.isInvincible()) return;


        if (enemy instanceof CrystallizedEnemy guardian && guardian.isFiringLaser()
            && guardian.getLaserHitbox().overlaps(knight.getHitbox())) {
            boolean laserDamaged = knight.applyKnockBack(guardian.getContactDamage(), guardian.getPosition().x);
            if (laserDamaged) {
                eventBus.post(GameEvent.KNIGHT_TOOK_DAMAGE);
                return;
            }
        }

        if (!enemy.getHitbox().overlaps(knight.getHitbox())) return;

        if (enemy instanceof FalseKnight fk && fk.getAttackHitbox().width > 0f) {
            return;
        }

        boolean damaged = knight.applyKnockBack(enemy.getContactDamage(), enemy.getPosition().x);
        if (damaged) {
            if (enemy instanceof HornHeadEnemy hornHead) {
                hornHead.onChargeHitPlayer();
            }
            eventBus.post(GameEvent.KNIGHT_TOOK_DAMAGE);
        }
    }
    private void handleDeathCleanup(Enemy enemy, Room room, int index) {
        room.removeEnemyAt(index);
        eventBus.post(GameEvent.ENEMY_DIED);
        EnemyType enemyType = resolveEnemyType(enemy);
        if (enemyType != null) {
            eventBus.post(GameEvent.ENEMY_KILLED, new GameEventPayload.EnemyKilled(enemyType));
        }
    }

    private EnemyType resolveEnemyType(Enemy enemy) {
        String typeName = enemy.getType();
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }
        try {
            return EnemyType.valueOf(typeName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
