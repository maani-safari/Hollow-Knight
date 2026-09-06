package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.github.HollowKnight.Model.entity.enemy.CrystallizedEnemy;
import io.github.HollowKnight.Model.entity.enemy.Enemy;
import io.github.HollowKnight.Model.entity.enemy.EnemyState;
import io.github.HollowKnight.Model.entity.enemy.HornHeadEnemy;

import java.util.HashMap;
import java.util.Map;

public class EnemyView {
    private Map<String, Animation<TextureRegion>> animations;
    private final Array<TextureAtlas> loadedAtlases;
    private float stateTime;
    private final Texture laserSheet;
    private final TextureRegion laserBeam;
    private final TextureRegion laserGlow;

    public EnemyView() {
        animations = new HashMap<>();
        loadedAtlases = new Array<>();
        loadAnimations("Mosscreep");
        loadAnimations("Mosquito");
        loadAnimations("Hornhead");
        loadAnimations("Crystallized");

        laserSheet = new Texture(Gdx.files.internal("effects/crystal_laser.png"));
        laserSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        laserBeam = new TextureRegion(laserSheet, 0, 16, 240, 48);
        laserGlow = new TextureRegion(laserSheet, 356, 34, 120, 96);
    }

    private void loadAnimations(String enemyName) {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("atlas/" + enemyName + ".atlas"));
        loadedAtlases.add(atlas);
        String upName = enemyName.toUpperCase();
        if (atlas.findRegions("Walk").size > 0)
            animations.put(upName + "_WALK", new Animation<>(0.1f, atlas.findRegions("Walk"), Animation.PlayMode.LOOP));
        if (atlas.findRegions("Death Land").size > 0)
            animations.put(upName + "_DEATH", new Animation<>(0.1f, atlas.findRegions("Death Land"), Animation.PlayMode.NORMAL));
        if (atlas.findRegions("Idle").size > 0)
            animations.put(upName + "_IDLE", new Animation<>(0.1f, atlas.findRegions("Idle"), Animation.PlayMode.LOOP));
        if (atlas.findRegions("Attack Anticipate").size > 0)
            animations.put(upName + "_ATTACK_ANTICIPATE", new Animation<>(0.1f, atlas.findRegions("Attack Anticipate"), Animation.PlayMode.NORMAL));
        if (atlas.findRegions("Attack").size > 0)
            animations.put(upName + "_ATTACK", new Animation<>(0.1f, atlas.findRegions("Attack"), Animation.PlayMode.NORMAL));
        if (atlas.findRegions("Attack Lunge").size > 0)
            animations.put(upName + "_ATTACK_LUNGE", new Animation<>(0.1f, atlas.findRegions("Attack Lunge"), Animation.PlayMode.NORMAL));
        if(atlas.findRegions("Run").size >0 )
            animations.put(upName + "_RUN" , new Animation<>(0.1f,atlas.findRegions("Run"), Animation.PlayMode.LOOP));
        if (atlas.findRegions("Shoot").size > 0)
            animations.put(upName + "_SHOOT" , new Animation<>(0.1f , atlas.findRegions("Shoot"), Animation.PlayMode.NORMAL));
        if (atlas.findRegions("Evade").size > 0)
            animations.put(upName + "_EVADE" , new Animation<>(0.1f , atlas.findRegions("Evade") , Animation.PlayMode.NORMAL));
        if (atlas.findRegions("Turn").size > 0)
            animations.put(upName + "_TURN", new Animation<>(0.15f, atlas.findRegions("Turn"), Animation.PlayMode.NORMAL));

    }

    public void render(SpriteBatch batch, Enemy enemy, float delta) {
        stateTime += delta;
        String keyName = getAnimationKey(enemy);
        Animation<TextureRegion> currentAnimation = null;

        if (enemy.getType().equals("MOSSCREEP")) {
            currentAnimation = animations.getOrDefault(keyName, animations.get("MOSSCREEP_IDLE"));
        } else if (enemy.getType().equals("MOSQUITO")) {
            currentAnimation = animations.getOrDefault(keyName, animations.get("MOSQUITO_WALK"));
        } else if (enemy.getType().equals("HORNHEAD")) {
            currentAnimation = animations.getOrDefault(keyName, animations.get("HORNHEAD_IDLE"));
        } else if (enemy.getType().equals("CRYSTALLIZED")) {
            currentAnimation = animations.getOrDefault(keyName, animations.get("CRYSTALLIZED_IDLE"));
        }
        boolean looping = enemy.getEnemyState() != EnemyState.DEATH;
        TextureRegion currentFrame = currentAnimation.getKeyFrame(enemy.getStateTimer(), looping);
        float scaleY = 1.5f;
        float scaleX = 3f;
        float width = enemy.getHitbox().getWidth() * scaleX;
        float height = enemy.getHitbox().getHeight() * scaleY;
        float drawX = enemy.getPosition().x - 80f;
        float drawY = enemy.getPosition().y;

        if (enemy.getType().equals("CRYSTALLIZED")) {
            if (enemy.isFacingRight()) {
                batch.draw(currentFrame, drawX + width, drawY, -width, height);
            } else {
                batch.draw(currentFrame, drawX, drawY, width, height);
            }
        } else {
            if (!enemy.isFacingRight() && currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            } else if (enemy.isFacingRight() && !currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
            batch.draw(currentFrame, drawX, drawY, width, height);
        }

        if (enemy instanceof CrystallizedEnemy guardian) {
            renderLaserEffects(batch, guardian);
        }
    }

    private void renderLaserEffects(SpriteBatch batch, CrystallizedEnemy guardian) {
        Rectangle hb = guardian.getHitbox();
        if (guardian.isCharging()) {
            float progress = guardian.getChargeProgress();
            float size = 20f + 60f * progress;
            float cx = guardian.isFacingRight() ? hb.x + hb.width : hb.x;
            float cy = hb.y + hb.height / 2f;
            batch.draw(laserGlow, cx - size / 2f, cy - size / 2f, size, size);
        } else if (guardian.isFiringLaser()) {
            Rectangle beam = guardian.getLaserHitbox();
            boolean flip = !guardian.isFacingRight();
            if (laserBeam.isFlipX() != flip) laserBeam.flip(true, false);
            float pad = beam.height * 0.5f;
            batch.draw(laserBeam, beam.x, beam.y - pad, beam.width, beam.height + pad * 2f);

            float glowSize = beam.height * 2.5f;
            float gx = guardian.isFacingRight() ? beam.x : beam.x + beam.width;
            batch.draw(laserGlow, gx - glowSize / 2f,
                beam.y + beam.height / 2f - glowSize / 2f, glowSize, glowSize);
        }
    }
    public void dispose() {
        for (TextureAtlas atlas : loadedAtlases) {
            if (atlas != null) atlas.dispose();
        }
        laserSheet.dispose();
    }
    private String getAnimationKey(Enemy enemy) {
        return switch (enemy.getType()) {
            case "HORNHEAD" -> {
                if (enemy instanceof HornHeadEnemy horn && enemy.getEnemyState() == EnemyState.ATTACKING) {
                    if (horn.isWindingUp()) yield "HORNHEAD_ATTACK_ANTICIPATE";
                    if (horn.isDashing()) yield "HORNHEAD_ATTACK_LUNGE";
                    yield "HORNHEAD_IDLE";
                }
                yield switch (enemy.getEnemyState()) {
                    case IDLE -> "HORNHEAD_IDLE";
                    case WALK -> "HORNHEAD_WALK";
                    case DEATH -> "HORNHEAD_DEATH";
                    default -> "HORNHEAD_WALK";
                };
            }


            case "MOSQUITO" -> switch (enemy.getEnemyState()) {
                case IDLE -> "MOSQUITO_IDLE";
                case ALERT -> "MOSQUITO_ATTACK_ANTICIPATE";
                case ATTACKING -> "MOSQUITO_ATTACK";
                case DEATH -> "MOSQUITO_DEATH";
                default -> "MOSQUITO_IDLE";
            };

            case "CRYSTALLIZED" -> {
                if (enemy instanceof CrystallizedEnemy guardian) {
                    if (guardian.isTurning()) yield "CRYSTALLIZED_TURN";
                    if (guardian.isEnrageRunning()) yield "CRYSTALLIZED_RUN";
                }
                yield switch (enemy.getEnemyState()) {
                    case IDLE, ALERT -> "CRYSTALLIZED_IDLE";
                    case WALK -> "CRYSTALLIZED_RUN";
                    case ATTACKING -> "CRYSTALLIZED_SHOOT";
                    case DEATH -> "CRYSTALLIZED_DEATH";
                    default -> "CRYSTALLIZED_IDLE";
                };
            }

            default -> switch (enemy.getEnemyState()) {
                case IDLE, WALK -> "MOSSCREEP_WALK";
                case DEATH -> "MOSSCREEP_DEATH";
                default -> "MOSSCREEP_WALK";
            };
        };
    }
}
