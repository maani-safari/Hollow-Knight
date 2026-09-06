package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import io.github.HollowKnight.Controller.SpellController;
import io.github.HollowKnight.Model.entity.HowlingWraithsEffect;
import io.github.HollowKnight.Model.entity.SpellProjectile;

public class SpellView {
    private final SpellController spellController;
    private final TextureAtlas atlas;
    private final Animation<TextureRegion> vengefulSpiritAnim;
    private final Animation<TextureRegion> howlingWraithsAnim;
    private static final float VENGEFUL_FRAME_DURATION = 0.05f;
    private static final float WRAITHS_FRAME_DURATION    = 0.08f;
    private static final float VENGEFUL_DRAW_SCALE       = 3f;
    private static final float WRAITHS_DRAW_SCALE         = 3f;

    public SpellView(SpellController spellController) {
        this.spellController = spellController;
        this.atlas = new TextureAtlas(Gdx.files.internal("atlas/Spells.atlas"));
        howlingWraithsAnim = new Animation<>(
            VENGEFUL_FRAME_DURATION,
            atlas.findRegions("SoulScream"),
            Animation.PlayMode.LOOP
        );
        vengefulSpiritAnim = new Animation<>(
            WRAITHS_FRAME_DURATION,
            atlas.findRegions("SoulBall"),
            Animation.PlayMode.NORMAL
        );
    }
    public void draw(SpriteBatch batch){
        drawProjectile(batch);
        drawHowlingWraiths(batch);
    }
    public void drawProjectile(SpriteBatch batch){
        for (SpellProjectile projectile : spellController.getActiveProjectiles()){
            Rectangle hb = projectile.getHitbox();
            float animTime = projectile.getLifeTimer();
            TextureRegion frame = vengefulSpiritAnim.getKeyFrame(animTime, true);
            float drawW = hb.width * VENGEFUL_DRAW_SCALE;
            float drawH = hb.height * VENGEFUL_DRAW_SCALE;
            float drawX = hb.x - (drawW - hb.width) / 2f;
            float drawY = hb.y - (drawH - hb.height) / 2f;
            if (!projectile.isFacingRight()) {
                if (!frame.isFlipX()) frame.flip(false, false);
                batch.draw(frame, drawX + drawW, drawY, -drawW, drawH);
            } else {
                if (frame.isFlipX()) frame.flip(true, false);
                batch.draw(frame, drawX, drawY, drawW, drawH);
            }
        }
    }
    public void drawHowlingWraiths(SpriteBatch batch){
        for (HowlingWraithsEffect wraith : spellController.getActiveWraiths()){
            Rectangle hb = wraith.getHitbox();
            float animTime = wraith.getTimer();
            TextureRegion frame = howlingWraithsAnim.getKeyFrame(animTime, false);
            float drawW = hb.width * WRAITHS_DRAW_SCALE;
            float drawH = hb.height * WRAITHS_DRAW_SCALE;
            float drawX = hb.x - (drawW - hb.width) / 2f;
            float drawY = hb.y - (drawH - hb.height) / 2f;
            batch.draw(frame, drawX, drawY, drawW, drawH);
        }
    }

    public void dispose() {
        if (atlas != null) atlas.dispose();
    }
}
