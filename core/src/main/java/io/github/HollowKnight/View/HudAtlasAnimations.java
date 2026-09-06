package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;


public class HudAtlasAnimations {

    public final TextureRegion hudFrame;
    public final TextureRegion soulOrbShape;
    public final TextureRegion soulOrbEyes;
    public final TextureRegion soulOrbGlow;
    public final TextureRegion healthBackboard;

    public final Animation<TextureRegion> maskIdle;
    public final Animation<TextureRegion> maskAppear;
    public final Animation<TextureRegion> maskRefill;
    public final Animation<TextureRegion> maskBreak;
    public final Animation<TextureRegion> soulFluidIdle;

    private final TextureAtlas atlas;

    public HudAtlasAnimations() {
        atlas = new TextureAtlas(Gdx.files.internal("hud/Hud.atlas"));

        hudFrame = findRegion("HUD Cln_HUD_frame_v020005");
        soulOrbShape = findRegion("HUD Cln_soul_orb_shape");
        soulOrbEyes = findRegion("HUD Cln_soul_orb_eyes");
        soulOrbGlow = findRegion("HUD Cln_soul_orb_glow0000");
        healthBackboard = findRegion("HUD Cln_health_backboard");

        maskIdle = buildAnimation(new String[]{
            "HUD Cln_idle_v020000",
            "HUD Cln_idle_v020001",
            "HUD Cln_idle_v020002",
            "HUD Cln_idle_v020003",
            "HUD Cln_idle_v020004",
            "HUD Cln_idle_v020005"
        }, 1f / 12f, Animation.PlayMode.LOOP);

        maskAppear = buildAnimation(new String[]{
            "HUD Cln_appear_v020000",
            "HUD Cln_appear_v020001",
            "HUD Cln_appear_v020002",
            "HUD Cln_appear_v020003",
            "HUD Cln_appear_v020004"
        }, 0.5f / 5f, Animation.PlayMode.NORMAL);

        maskRefill = buildAnimation(new String[]{
            "HUD Cln_refill0000",
            "HUD Cln_refill0001",
            "HUD Cln_refill0002",
            "HUD Cln_refill0003",
            "HUD Cln_refill0004",
            "HUD Cln_refill0005"
        }, 0.5f / 6f, Animation.PlayMode.NORMAL);

        maskBreak = buildAnimation(new String[]{
            "HUD Cln_break_backboard0000",
            "HUD Cln_break_backboard0001",
            "HUD Cln_break_backboard0002",
            "HUD Cln_break_backboard0003",
            "HUD Cln_break_backboard0004",
            "HUD Cln_break_backboard0005"
        }, 1f / 18f, Animation.PlayMode.NORMAL);

        soulFluidIdle = buildPrefixAnimation("HUD_Soulorb_fills_soul_idle", 1f / 12f, Animation.PlayMode.LOOP);
    }

    private TextureRegion findRegion(String name) {
        TextureRegion region = atlas.findRegion(name);
        if (region == null) {
            Gdx.app.error("HudAtlas", "Missing atlas region: " + name);
        }
        return region;
    }

    private Animation<TextureRegion> buildAnimation(String[] frameNames, float frameDuration,
                                                    Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>(frameNames.length);
        for (String name : frameNames) {
            TextureRegion region = findRegion(name);
            if (region != null) {
                frames.add(region);
            }
        }
        if (frames.size == 0) {
            Gdx.app.error("HudAtlas", "Animation has no valid frames starting at: " + frameNames[0]);
            return null;
        }
        return new Animation<>(frameDuration, frames, playMode);
    }

    private Animation<TextureRegion> buildPrefixAnimation(String prefix, float frameDuration,
                                                          Animation.PlayMode playMode) {
        Array<TextureAtlas.AtlasRegion> frames = new Array<>();
        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
            if (region.name.startsWith(prefix)) {
                frames.add(region);
            }
        }
        frames.sort(Comparator.comparing(r -> r.name));
        if (frames.size == 0) {
            Gdx.app.error("HudAtlas", "No regions found for prefix: " + prefix);
            return null;
        }
        return new Animation<>(frameDuration, frames, playMode);
    }

    public void dispose() {
        if (atlas != null) {
            atlas.dispose();
        }
    }
}
