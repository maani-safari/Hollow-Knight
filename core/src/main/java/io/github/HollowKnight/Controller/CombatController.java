package io.github.HollowKnight.Controller;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.github.HollowKnight.Model.World.BreakableWall;
import io.github.HollowKnight.Model.World.Hazard;
import io.github.HollowKnight.Model.World.Room;
import io.github.HollowKnight.Model.entity.Direction;
import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.entity.enemy.Enemy;
import io.github.HollowKnight.Model.entity.enemy.EnemyState;
import io.github.HollowKnight.Model.entity.enemy.FalseKnight;

public class CombatController {
    public static final float NAIL_RANGE_HOR = 50f;
    public static final float NAIL_RANGE_VER = 150f;
    public static final float NAIL_THICKNESS = 20f;
    public static final float POGO_VEL = 300f;
    private final GameEventBus eventBus;
    private NpcInteractionController npcInteractionController;

    public CombatController(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setNpcInteractionController(NpcInteractionController npcInteractionController) {
        this.npcInteractionController = npcInteractionController;
    }

    public void handleNailAttack(Knight knight , Direction dir , Room room){
        Rectangle hitbox = attackHitbox(knight , dir);
        boolean hitEnemy = hitEnemy(knight , hitbox , room,false);
        boolean hitNpc = hitNpc(hitbox, room);
        boolean hitHazard = hitHazard(hitbox,room);
//        hit |= hitBreakableWall();
        if (hitEnemy) {
            eventBus.post(GameEvent.KNIGHT_HIT_ENEMY);
        }
        if (hitEnemy || hitHazard) {
            if (dir == Direction.DOWN && (hitEnemy || hitHazard)) {
                knight.getVelocity().y = POGO_VEL;
                knight.onPogoSuccess();
            }
        }
    }
    public Rectangle attackHitbox(Knight knight, Direction dir) {
        Rectangle knightBox = knight.getHitbox();
        float cx = knightBox.x + knightBox.width / 2f;
        float cy = knightBox.y + knightBox.height / 2f;
        return switch (dir) {
            case RIGHT ->
                new Rectangle(knightBox.x + knightBox.width, cy - NAIL_THICKNESS / 2f, NAIL_RANGE_HOR, NAIL_THICKNESS);
            case LEFT ->
                new Rectangle(knightBox.x - NAIL_RANGE_VER, cy - NAIL_THICKNESS / 2f, NAIL_RANGE_HOR, NAIL_THICKNESS);
            case UP ->
                new Rectangle(cx - NAIL_THICKNESS / 2f, knightBox.y + knightBox.height, NAIL_THICKNESS, NAIL_RANGE_VER);
            case DOWN ->
                new Rectangle(cx - NAIL_THICKNESS / 2f, knightBox.y - NAIL_RANGE_VER, NAIL_THICKNESS, NAIL_RANGE_VER);
        };
    }
    private boolean hitEnemy(Knight knight , Rectangle hitBox , Room room , boolean isPogo){
        boolean hit = false;
        Array<Enemy> enemies = room.getEnemies();
        for (Enemy enemy : enemies){
            if (enemy.getEnemyState()== EnemyState.DEATH) continue;
            if (enemy instanceof FalseKnight fk) {
                if (fk.isWeakSpotActive()) {
                    if (hitBox.overlaps(fk.getWeakSpotHitbox())) {
                        int damage = knight.getNailDamage();
                        enemy.onHit(damage);
                        if (knight.gainSoul() > 0) {
                            eventBus.post(GameEvent.KNIGHT_GAINED_SOUL);
                        }
                        hit = true;
                    }
                    continue;
                }
            }
            if (!hitBox.overlaps(enemy.getHitbox())) continue;
            int damage = knight.getNailDamage();
            enemy.onHit(damage);
            if (knight.gainSoul() > 0) {
                eventBus.post(GameEvent.KNIGHT_GAINED_SOUL);
            }
            hit = true;
        }
        return hit;
    }
    private boolean hitNpc(Rectangle hitbox, Room room) {
        if (npcInteractionController == null) {
            return false;
        }
        return npcInteractionController.tryHitNpc(hitbox, room);
    }
    private boolean hitBreakableWalls(Rectangle hitbox, Room room) {
        boolean hitAny = false;
        for (BreakableWall wall : room.getBreakableWalls()) {
            if (wall.isBroken()) continue;
            if (!hitbox.overlaps(wall.getHitbox())) continue;

//            worldController.hitBreakableWall(wall);
            hitAny = true;
        }
        return hitAny;
    }
    private boolean hitHazard(Rectangle hitbox,Room room){
        boolean hitHazard = false;
        for (Hazard hazard : room.getHazards()){
            if (!hitbox.overlaps(hazard.getHitbox())) continue;
            hitHazard = true;
        }
        return hitHazard;
    }
}
