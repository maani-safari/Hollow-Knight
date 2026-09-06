package io.github.HollowKnight.Controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.HollowKnight.Model.World.Hazard;
import io.github.HollowKnight.Model.World.Room;
import io.github.HollowKnight.Model.World.BreakableWall;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.World.SafeSpawn;
import io.github.HollowKnight.Model.entity.Direction;
import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.entity.MovementState;

public class KnightController {
    public static final float GRAVITY = -800f;
    public static final float MAX_FALL_SPEED = -500f;
    public static final float MOVE_SPEED = 200f;
    public static final float NOCLIP_SPEED = 350f;
    public static final float DASH_SPEED = 1500f;
    public static final float JUMP_SPEED = 600f;
    public static final float DOUBLE_JUMP_SPEED = 650f;
    public static final float JUMP_CUT = 0.4f;
    public static final float WALL_JUMP_X = 300f;
    public static final float WALL_JUMP_Y = 400f;
    public static final float HURT_KNOCKBACK_X = 150f;
    public static final float HURT_KNOCKBACK_Y = 200f;
    public static final float WALL_SLIDE_SPEED = -200f;
    private static final float WALL_JUMP_DURATION = 0.3f;

    private final Knight knight;
    private final KeyController keys;
    private final GameEventBus eventBus;
    private boolean wallJump;
    private float wallJumpTimer;
    private final CombatController combatController;
    private final SpellController spellController;
    public KnightController(Knight knight, KeyController keys, CombatController combatController , SpellController spellController , GameEventBus eventBus) {
        this.knight = knight;
        this.keys = keys;
        this.eventBus = eventBus;
        this.combatController = combatController;
        this.spellController = spellController;

    }

    public void update(float delta, Room room) {
        tickTimers(delta);
        if (Gdx.input.isKeyJustPressed(keys.getEmergency())
            && Gdx.input.isKeyPressed(keys.getCheatMode())
            && !knight.isAlive()) {
            knight.increaseHealthMask();
            knight.setAlive(true);
            knight.setMovementState(MovementState.IDLE);
        }
        if (!knight.isAlive()) {
            return;
        }
        if (knight.isDialogueLocked()) {
            knight.getVelocity().setZero();
            tickTimers(delta);
            return;
        }
        if (Gdx.input.isKeyJustPressed(keys.getBossTeleport())
            && Gdx.input.isKeyPressed(keys.getCheatMode())) {
            eventBus.post(GameEvent.CHEAT_BOSS_TELEPORT);

        }
        if (Gdx.input.isKeyPressed(keys.getCheatMode()) &&
            Gdx.input.isKeyPressed(keys.getGodStart()) &&
         !knight.isGodMode()){
            knight.activateGodMode();
        }
        if (Gdx.input.isKeyPressed(keys.getCheatMode()) &&
            Gdx.input.isKeyPressed(keys.getGodExit()) &&
            knight.isGodMode()){
            knight.deactivateGodMode();
        }
        if (Gdx.input.isKeyPressed(keys.getCheatMode()) &&
            Gdx.input.isKeyPressed(keys.getHesoyam())){
            knight.fillSoulAndHealth();
        }
        if (Gdx.input.isKeyPressed(keys.getCheatMode())
            && Gdx.input.isKeyPressed(keys.getNoclipStart())
            && !knight.isNoclip()) {
            knight.activateNoclip();
        }
        if (knight.isNoclip()) {
            updateNoclip(delta);
            return;
        }
        handleJump(delta);
        handleMovement(delta);
        handleDash(delta);
        handleGravity(delta);
        handleFucus(delta);
        handleCollisions(delta, room);
        handleAttack(room);
        handleSpell(room);
        knight.updateMovementState();
    }
    private void teleportToBossArena(Room room) {
        Vector2 entry = room.getBossArenaEntryPoint();
        if (entry == null) return;

        knight.setPosition(new Vector2(entry.x, entry.y));
        knight.getVelocity().set(0, 0);
        knight.syncHitbox();
        knight.setLastSafeX(entry.x);
        knight.setLastSafeY(entry.y);
        eventBus.post(GameEvent.CHEAT_ACTIVATED);
    }
    public void updateNoclip(float delta){
        if (Gdx.input.isKeyPressed(keys.getCheatMode())
            && Gdx.input.isKeyPressed(keys.getNoclipExit())) {
            knight.deactivateNoclip();
            return;
        }
        float vx = 0, vy = 0;
        if (Gdx.input.isKeyPressed(keys.getMoveLeft()))  vx = -NOCLIP_SPEED;
        if (Gdx.input.isKeyPressed(keys.getMoveRight())) vx =  NOCLIP_SPEED;
        if (Gdx.input.isKeyPressed(keys.getJump()))      vy =  NOCLIP_SPEED;
        if (Gdx.input.isKeyPressed(keys.getMoveDown()))  vy = -NOCLIP_SPEED;
        knight.setVelocity(new Vector2(vx, vy));
        knight.getPosition().mulAdd(knight.getVelocity(), delta);
        knight.syncHitbox();
    }
    public void tickTimers(float delta) {
        knight.tickAttack(delta);
        knight.tickDash(delta);
        knight.tickFocus(delta);
        knight.tickHurt(delta);
        knight.tickInvincibility(delta);
        knight.tickDashCooldown(delta);
        if (wallJump) {
            wallJumpTimer -= delta;
            if (wallJumpTimer <= 0)
                wallJump = false;
        }
    }

    public void handleGravity(float delta) {
        if (knight.isDashing() || knight.isGround()) return;
        if (knight.isWallSliding()) {
            knight.getVelocity().y = Math.max(WALL_SLIDE_SPEED, knight.getVelocity().y + GRAVITY * delta);
        } else {
            knight.getVelocity().y = Math.max(MAX_FALL_SPEED, knight.getVelocity().y + GRAVITY * delta);
        }
    }

    public void handleJump(float delta) {
        boolean jumpHeld = Gdx.input.isKeyPressed(keys.getJump());
        boolean jumpJustPressed = Gdx.input.isKeyJustPressed(keys.getJump());
        if (knight.isFocus()) {
            knight.getVelocity().setZero();
            return;
        }
        if (!jumpHeld && !knight.isGround() && knight.getVelocity().y > 0 && knight.getMovementState() == MovementState.JUMPING) {
            knight.getVelocity().y *= JUMP_CUT;
        }

        if (!jumpJustPressed) return;

        if (knight.isWallSliding()) {
            doWallJump();
            return;
        }
        if (knight.canJump()) {
            doJump();
            return;
        }
        if (knight.canDoubleJump()) {
            doDoubleJump();
        }
    }

    private void doJump() {
        knight.getVelocity().y = JUMP_SPEED;
        knight.leaveGround();
        knight.setMovementState(MovementState.JUMPING);
        eventBus.post(GameEvent.KNIGHT_JUMPED);
    }

    private void doDoubleJump() {
        knight.getVelocity().y = DOUBLE_JUMP_SPEED;
        knight.consumeDoubleJump();
        knight.setMovementState(MovementState.JUMPING);
        eventBus.post(GameEvent.KNIGHT_DOUBLE_JUMPED);

    }

    private void doWallJump() {

        float dirX = knight.isTouchingWallLeft() ? WALL_JUMP_X : -WALL_JUMP_X;
        knight.getVelocity().set(dirX, WALL_JUMP_Y);
        knight.setFacingRight(dirX > 0);
        knight.setMovementState(MovementState.JUMPING);
        wallJump = true;
        wallJumpTimer = WALL_JUMP_DURATION;
        eventBus.post(GameEvent.KNIGHT_WALL_JUMPED);
    }

    public void handleMovement(float delta) {
        if (knight.isHurt() || knight.isFocus() || knight.isDashing()) return;
        boolean left = Gdx.input.isKeyPressed(keys.getMoveLeft());
        boolean right = Gdx.input.isKeyPressed(keys.getMoveRight());

        if (wallJump) {
            if (knight.isTouchingWallLeft()) left = false;
            if (knight.isTouchingWallRight()) right = false;
        }

        float speed = MOVE_SPEED;
        if (left) {
            knight.getVelocity().x = -speed;
            knight.setFacingRight(false);
        } else if (right) {
            knight.getVelocity().x = speed;
            knight.setFacingRight(true);
        } else {
            knight.getVelocity().x = 0;
        }
    }

    public void handleDash(float delta) {
        if (knight.isFocus()) {
            knight.getVelocity().setZero();
            return;
        }
        boolean dashJustPressed = Gdx.input.isKeyJustPressed(keys.getDash());
        if (!dashJustPressed) {
            if (knight.isDashing()) {
                float dist = DASH_SPEED * knight.getCharmState().dashDistanceMultiplier;
                knight.getVelocity().x = knight.isFacingRight() ? dist : -dist;
                knight.getVelocity().y = 0f;
            }
        } else if (knight.canDashStart()) {
            Direction dir = knight.isFacingRight() ? Direction.RIGHT : Direction.LEFT;
            knight.startDash(dir);
            eventBus.post(GameEvent.KNIGHT_DASHED);
        }
    }

    public void handleFucus(float delta) {
        boolean focus = Gdx.input.isKeyPressed(keys.getFocus());
        if (focus) {
            if (!knight.isFocus())
                knight.startFocus();
            boolean healed = knight.tickFocus(delta);
            if (healed)
                eventBus.post(GameEvent.KNIGHT_HEALED);
        } else {
            if (knight.isFocus())
                knight.cancelFocus();
        }
    }

    private void handleCollisions(float delta, Room room) {
        if (knight.isFocus()) {
            knight.getVelocity().setZero();
            return;
        }
        knight.getPosition().x += knight.getVelocity().x * delta;
        knight.syncHitbox();
        resolveHorizontalCollisions(room);

        knight.getPosition().y += knight.getVelocity().y * delta;
        knight.syncHitbox();
        resolveVerticalCollisions(room);

        detectWallContact(room);
        updateSafeSpawnCheckpoint(room);
        checkHazardCollision(room);
    }
    private void handleAttack(Room room){

        boolean justPressed = Gdx.input.isKeyJustPressed(keys.getAttack());
        if (!justPressed || !knight.canAttack()) return;
        Direction dir = resolveAttackDirection();
        knight.startAttack(dir);
        combatController.handleNailAttack(knight, dir, room);
        eventBus.post(GameEvent.KNIGHT_ATTACKED);

    }
    private void handleSpell(Room room){
        boolean fHeld = Gdx.input.isKeyJustPressed(keys.getVengeful());
        boolean vHeld = Gdx.input.isKeyJustPressed(keys.getHowling());
        if ( vHeld && !knight.isGround()) {
            spellController.castHowlingWraiths(knight);
            return;
        }
        if (fHeld) {
            spellController.castVengefulSpirit(knight);
        }
    }
    private Direction resolveAttackDirection() {
        if (Gdx.input.isKeyPressed(keys.getMoveUp()))    return Direction.UP;
        if (Gdx.input.isKeyPressed(keys.getMoveDown())) return Direction.DOWN;
        return knight.isFacingRight() ? Direction.RIGHT : Direction.LEFT;
    }

    private void resolveHorizontalCollisions(Room room) {
        Rectangle kBox = knight.getHitbox();

        for (Rectangle platform : room.getPlatforms()) {
            if (!kBox.overlaps(platform)) continue;
            adjustHorizontalPosition(platform, kBox);
            kBox = knight.getHitbox();
        }

        for (BreakableWall wall : room.getBreakableWalls()) {
            if (wall.isBroken() || !kBox.overlaps(wall.getHitbox())) continue;
            adjustHorizontalPosition(wall.getHitbox(), kBox);
        }

        for (Rectangle arenaWall : room.getArenaWalls()) {
            if (!kBox.overlaps(arenaWall)) continue;
            adjustHorizontalPosition(arenaWall, kBox);
            kBox = knight.getHitbox();
        }
    }

    private void adjustHorizontalPosition(Rectangle obstacle, Rectangle kBox) {
        if (knight.getVelocity().x > 0) {
            knight.getPosition().x = obstacle.x - kBox.width;
        } else if (knight.getVelocity().x < 0) {
            knight.getPosition().x = obstacle.x + obstacle.width;
        }
        knight.getVelocity().x = 0;
        knight.syncHitbox();
    }

    private void resolveVerticalCollisions(Room room) {
        Rectangle kBox = knight.getHitbox();
        boolean grounded = false;
        for (Rectangle platform : room.getPlatforms()) {
            if (!kBox.overlaps(platform)) continue;
            grounded |= adjustVerticalPosition(platform, kBox);
            kBox = knight.getHitbox();
        }

        for (BreakableWall wall : room.getBreakableWalls()) {
            if (wall.isBroken() || !kBox.overlaps(wall.getHitbox())) continue;
            grounded |= adjustVerticalPosition(wall.getHitbox(), kBox);
            kBox = knight.getHitbox();
        }

        for (Rectangle arenaWall : room.getArenaWalls()) {
            if (!kBox.overlaps(arenaWall)) continue;
            grounded |= adjustVerticalPosition(arenaWall, kBox);
            kBox = knight.getHitbox();
        }

        if (grounded && !knight.isGround()) {
            knight.onLand();
        } else if (!grounded && knight.isGround()) {
            knight.leaveGround();
        }
        knight.setGround(grounded);
    }

    private boolean adjustVerticalPosition(Rectangle obstacle, Rectangle kBox) {
        boolean hitGround = false;
        if (knight.getVelocity().y <= 0) {
            knight.getPosition().y = obstacle.y + obstacle.height;
            knight.getVelocity().y = 0;
            hitGround = true;
        } else if (knight.getVelocity().y > 0) {
            knight.getPosition().y = obstacle.y - kBox.height;
            knight.getVelocity().y = 0;
        }
        knight.syncHitbox();
        return hitGround;
    }

    private void detectWallContact(Room room) {
        Rectangle kBox = knight.getHitbox();
        boolean left = false;
        boolean right = false;

        Rectangle probeL = new Rectangle(kBox.x - 1, kBox.y + 2, 1, kBox.height - 4);
        Rectangle probeR = new Rectangle(kBox.x + kBox.width, kBox.y + 2, 1, kBox.height - 4);

        for (Rectangle platform : room.getPlatforms()) {
            if (probeL.overlaps(platform)) left = true;
            if (probeR.overlaps(platform)) right = true;
        }

        for (BreakableWall wall : room.getBreakableWalls()) {
            if (wall.isBroken()) continue;
            if (probeL.overlaps(wall.getHitbox())) left = true;
            if (probeR.overlaps(wall.getHitbox())) right = true;
        }

        for (Rectangle arenaWall : room.getArenaWalls()) {
            if (probeL.overlaps(arenaWall)) left = true;
            if (probeR.overlaps(arenaWall)) right = true;
        }

        knight.setTouchingWall(left, right);
    }

    private void updateSafeSpawnCheckpoint(Room room) {
        if (!knight.isGround() || !knight.isAlive()) return;

        for (SafeSpawn spawn : room.getSafeSpawns()) {
            if (knight.getHitbox().overlaps(spawn.getBounds())) {
                Rectangle r = spawn.getBounds();
                knight.setLastSafeX(r.x + r.width/2f);
                knight.setLastSafeY(r.y + 100f);

            }
        }
    }
    private void checkHazardCollision(Room room) {
        Rectangle kBox = knight.getHitbox();
        for (Hazard hazard : room.getHazards()) {
            if (kBox.overlaps(hazard.getHitbox())) {
                boolean damaged = knight.applyDamage(hazard.getDamage());
                if (damaged) {
                    knight.setVelocity(new Vector2(0, 0));
                    if (knight.isAlive()) {
                        knight.setPosition(new Vector2(knight.getLastSafeX(), knight.getLastSafeY()));
                        knight.syncHitbox();
                    }
                    eventBus.post(GameEvent.KNIGHT_HIT_HAZARD);
                    eventBus.post(GameEvent.KNIGHT_TOOK_DAMAGE);
                }
                break;
            }
        }
    }
}
