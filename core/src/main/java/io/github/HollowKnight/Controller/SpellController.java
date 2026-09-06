package io.github.HollowKnight.Controller;

import com.badlogic.gdx.Gdx;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.SoulVessel;
import io.github.HollowKnight.Model.World.Room;
import io.github.HollowKnight.Model.entity.HowlingWraithsEffect;
import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.entity.SpellProjectile;
import io.github.HollowKnight.Model.entity.enemy.Enemy;
import io.github.HollowKnight.Model.entity.enemy.EnemyState;

import java.util.ArrayList;
import java.util.List;

public class SpellController {
    public static final int VENGEFUL_SPIRIT_DAMAGE = 15;
    public static final int HOWLING_WRAITHS_DAMAGE  = 13;

    private final GameEventBus eventBus;

    private final List<SpellProjectile> activeProjectiles = new ArrayList<>();
    private final List<HowlingWraithsEffect> activeWraiths      = new ArrayList<>();
    private KeyController keys;
    public SpellController(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public boolean castVengefulSpirit(Knight knight) {
        if (!knight.getSoulVessel().canSpell()) return false;
        if (!knight.getSoulVessel().consume(spellCost(knight))) return false;

        float startX = knight.isFacingRight()
            ? knight.getHitbox().x + knight.getHitbox().width
            : knight.getHitbox().x;
        float startY = knight.getHitbox().y + knight.getHitbox().height / 2f - 5f;

        int damage = scaledDamage(knight, VENGEFUL_SPIRIT_DAMAGE);
        activeProjectiles.add(new SpellProjectile(startX, startY, knight.isFacingRight(), damage));

        eventBus.post(GameEvent.VENGEFUL_SPIRIT_CAST);
        eventBus.post(GameEvent.KNIGHT_CAST_SPELL);
        return true;
    }

    public boolean castHowlingWraiths(Knight knight) {
        if (!knight.getSoulVessel().canSpell()) return false;
        if (!knight.getSoulVessel().consume(spellCost(knight))) return false;

        int damagePerHit = scaledDamage(knight, HOWLING_WRAITHS_DAMAGE);
        activeWraiths.add(new HowlingWraithsEffect(
            knight.getPosition().x, knight.getHitbox().y + knight.getHitbox().height,
            knight.getHitbox().width, damagePerHit
        ));

        eventBus.post(GameEvent.HOWLING_WRAITHS_CAST);
        eventBus.post(GameEvent.KNIGHT_CAST_SPELL);
        return true;
    }

    private int spellCost(Knight knight) {
        return SoulVessel.SPELL_COST;
    }

    private int scaledDamage(Knight knight, int baseDamage) {
        return Math.round(baseDamage * knight.getCharmState().spellDamageMultiplier);
    }


    public void update(float delta, Room room) {
        updateProjectiles(delta, room);
        updateWraiths(delta, room);
    }

    private void updateProjectiles(float delta, Room room) {
        for (int i = activeProjectiles.size() - 1; i >= 0; i--) {
            SpellProjectile proj = activeProjectiles.get(i);
            proj.update(delta);

            if (!proj.isExpired()) {
                resolveProjectileHits(proj, room);
                if (hitsWall(proj, room)) proj.expire();
            }

            if (proj.isExpired()) {
                activeProjectiles.remove(i);
            }
        }
    }

    private void resolveProjectileHits(SpellProjectile proj, Room room) {
        for (Enemy enemy : room.getEnemies()) {
            if (enemy.getEnemyState() == EnemyState.DEATH) continue;
            if (proj.getHitbox().overlaps(enemy.getHitbox())) {
                enemy.onHit(proj.getDamage());
                proj.expire();
                break;
            }
        }
    }

    private boolean hitsWall(SpellProjectile proj, Room room) {
        for (var platform : room.getPlatforms()) {
            if (proj.getHitbox().overlaps(platform)) return true;
        }
        return false;
    }

    private void updateWraiths(float delta, Room room) {
        for (int i = activeWraiths.size() - 1; i >= 0; i--) {
            HowlingWraithsEffect wraith = activeWraiths.get(i);
            wraith.update(delta);

            if (wraith.consumePendingHit()) {
                applyWraithPulse(wraith, room);
            }

            if (wraith.isExpired()) {
                activeWraiths.remove(i);
            }
        }
    }

    private void applyWraithPulse(HowlingWraithsEffect wraith, Room room) {
        for (Enemy enemy : room.getEnemies()) {
            if (enemy.getEnemyState() == EnemyState.DEATH) continue;
            if (wraith.getHitbox().overlaps(enemy.getHitbox())) {
                enemy.onHit(wraith.getDamagePerHit());
            }
        }
    }


    public List<SpellProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    public List<HowlingWraithsEffect> getActiveWraiths() {
        return activeWraiths;
    }

    public void clearActiveEffects() {
        activeProjectiles.clear();
        activeWraiths.clear();
    }
}
