package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

public class HudRenderer {

    private static final float HUD_SCALE = 0.5f;
    private static final int MAX_MASKS = 5;
    private static final int MAX_SOUL = 99;
    private static final int FOCUS_SOUL_THRESHOLD = 33;
    private static final float INTRO_DURATION = 0.75f;
    private static final float MASK_STAGGER = 0.12f;

    private static final float ORB_X = 30f;
    private static final float ORB_Y_OFFSET = 110f;
    private static final float ORB_DRAW_X_OFFSET = 7f;

    private enum SlotPhase {
        HIDDEN, APPEARING, IDLE, BREAKING, REFILLING
    }

    private static final class SlotState {
        SlotPhase phase = SlotPhase.HIDDEN;
        float animTime;
    }

    private final HudAtlasAnimations animations;
    private final SlotState[] slots = new SlotState[MAX_MASKS];
    private final OrthographicCamera fboCamera = new OrthographicCamera();

    private FrameBuffer soulFbo;
    private TextureRegion soulFboRegion;
    private int fboWidth;
    private int fboHeight;

    private float introTimer;
    private int targetSoul;
    private int displaySoul;
    private int introTargetMasks;
    private int trackedMasks;
    private float maskIdleTime;
    private float soulFluidTime;

    public HudRenderer() {
        animations = new HudAtlasAnimations();
        for (int i = 0; i < MAX_MASKS; i++) {
            slots[i] = new SlotState();
        }
        initSoulFbo();
    }

    private void initSoulFbo() {
        if (animations.soulOrbShape == null) {
            return;
        }
        fboWidth = animations.soulOrbShape.getRegionWidth();
        fboHeight = animations.soulOrbShape.getRegionHeight();
        if (soulFbo != null) {
            soulFbo.dispose();
        }
        soulFbo = new FrameBuffer(Pixmap.Format.RGBA8888, fboWidth, fboHeight, false);
        soulFboRegion = new TextureRegion(soulFbo.getColorBufferTexture());
        soulFboRegion.flip(false, true);
        fboCamera.setToOrtho(false, fboWidth, fboHeight);
        fboCamera.update();
    }

    public void resize(int width, int height) {
    }

    public void onScreenShown(int soul, int masks) {
        introTimer = INTRO_DURATION;
        targetSoul = MathUtils.clamp(soul, 0, MAX_SOUL);
        displaySoul = 0;
        introTargetMasks = MathUtils.clamp(masks, 0, MAX_MASKS);
        trackedMasks = introTargetMasks;
        maskIdleTime = 0f;
        soulFluidTime = 0f;
        for (int i = 0; i < MAX_MASKS; i++) {
            slots[i].phase = SlotPhase.HIDDEN;
            slots[i].animTime = 0f;
        }
    }

    public void update(int masks, int soul, float delta) {
        masks = MathUtils.clamp(masks, 0, MAX_MASKS);
        soul = MathUtils.clamp(soul, 0, MAX_SOUL);
        maskIdleTime += delta;
        soulFluidTime += delta;

        if (introTimer > 0f) {
            introTimer = Math.max(0f, introTimer - delta);
            float elapsed = INTRO_DURATION - introTimer;
            displaySoul = (int) (targetSoul * Math.min(1f, elapsed / INTRO_DURATION));

            for (int i = 0; i < introTargetMasks; i++) {
                if (slots[i].phase == SlotPhase.HIDDEN && elapsed >= i * MASK_STAGGER) {
                    slots[i].phase = SlotPhase.APPEARING;
                    slots[i].animTime = 0f;
                }
            }
        } else {
            displaySoul = soul;

            if (masks < trackedMasks) {
                startBreak(trackedMasks - 1);
            } else if (masks > trackedMasks) {
                startRefill(masks - 1);
            }
            trackedMasks = masks;
        }

        updateSlotAnimations(delta);
    }

    private void startBreak(int index) {
        if (index < 0 || index >= MAX_MASKS) {
            return;
        }
        slots[index].phase = SlotPhase.BREAKING;
        slots[index].animTime = 0f;
    }

    private void startRefill(int index) {
        if (index < 0 || index >= MAX_MASKS) {
            return;
        }
        slots[index].phase = SlotPhase.REFILLING;
        slots[index].animTime = 0f;
    }

    private void updateSlotAnimations(float delta) {
        for (int i = 0; i < MAX_MASKS; i++) {
            SlotState slot = slots[i];
            switch (slot.phase) {
                case APPEARING -> {
                    slot.animTime += delta;
                    if (animations.maskAppear != null
                        && animations.maskAppear.isAnimationFinished(slot.animTime)) {
                        slot.phase = SlotPhase.IDLE;
                        slot.animTime = 0f;
                    }
                }
                case BREAKING -> {
                    slot.animTime += delta;
                    if (animations.maskBreak != null
                        && animations.maskBreak.isAnimationFinished(slot.animTime)) {
                        slot.phase = SlotPhase.HIDDEN;
                        slot.animTime = 0f;
                    }
                }
                case REFILLING -> {
                    slot.animTime += delta;
                    if (animations.maskRefill != null
                        && animations.maskRefill.isAnimationFinished(slot.animTime)) {
                        slot.phase = SlotPhase.IDLE;
                        slot.animTime = 0f;
                    }
                }
                default -> { }
            }
        }
    }

    public void render(SpriteBatch batch, int screenWidth, int screenHeight, int masks, int soul) {
        float orbX = ORB_X;
        float orbY = screenHeight - ORB_Y_OFFSET;

        if (animations.hudFrame != null) {
            drawRegion(batch, animations.hudFrame, orbX, orbY);
        }

        renderSoulVessel(batch, orbX, orbY, displaySoul, introTimer > 0f);

        float baseX = orbX - 11f;
        float baseY = orbY - 10f;
        masks = MathUtils.clamp(masks, 0, MAX_MASKS);

        for (int i = 0; i < MAX_MASKS; i++) {
            float slotX = baseX + 210f * HUD_SCALE + i * 80f * HUD_SCALE;
            float slotY = baseY + 35f * HUD_SCALE;
            SlotState slot = slots[i];

            if (animations.healthBackboard != null) {
                drawRegion(batch, animations.healthBackboard, slotX, slotY);
            }

            switch (slot.phase) {
                case BREAKING -> drawAnimation(batch, animations.maskBreak, slot.animTime, slotX, slotY);
                case REFILLING -> drawAnimation(batch, animations.maskRefill, slot.animTime, slotX, slotY);
                case APPEARING -> drawAnimation(batch, animations.maskAppear, slot.animTime, slotX, slotY);
                case IDLE -> {
                    if (i < masks) {
                        drawAnimation(batch, animations.maskIdle, maskIdleTime, slotX, slotY);
                    }
                }
                default -> {
                    if (i < masks && introTimer <= 0f) {
                        drawAnimation(batch, animations.maskIdle, maskIdleTime, slotX, slotY);
                    }
                }
            }
        }
    }

    private void renderSoulVessel(SpriteBatch batch, float orbX, float orbY, int soul, boolean inIntro) {
        if (soulFbo == null || animations.soulOrbShape == null) {
            return;
        }

        boolean batchWasDrawing = batch.isDrawing();
        Matrix4 screenMatrix = batch.getProjectionMatrix().cpy();
        if (batchWasDrawing) {
            batch.end();
        }

        float fillRatio = soul / (float) MAX_SOUL;
        renderSoulFluidToFbo(batch, fillRatio);

        batch.setProjectionMatrix(screenMatrix);
        if (batchWasDrawing) {
            batch.begin();
        }

        float drawX = orbX + ORB_DRAW_X_OFFSET;
        float drawW = fboWidth * HUD_SCALE;
        float drawH = fboHeight * HUD_SCALE;
        batch.draw(soulFboRegion, drawX, orbY, drawW, drawH);

        if (soul >= MAX_SOUL && animations.soulOrbGlow != null) {
            float glowW = animations.soulOrbGlow.getRegionWidth() * HUD_SCALE;
            float glowH = animations.soulOrbGlow.getRegionHeight() * HUD_SCALE;
            float glowX = drawX + (drawW - glowW) * 0.5f;
            float glowY = orbY + (drawH - glowH) * 0.5f;
            batch.draw(animations.soulOrbGlow, glowX, glowY, glowW, glowH);
        }

        if (soul >= FOCUS_SOUL_THRESHOLD && !inIntro && animations.soulOrbEyes != null) {
            float eyesW = animations.soulOrbEyes.getRegionWidth() * HUD_SCALE;
            float eyesH = animations.soulOrbEyes.getRegionHeight() * HUD_SCALE;
            batch.draw(animations.soulOrbEyes, drawX + 9f, orbY + 10f, eyesW, eyesH);
        }
    }

    private void renderSoulFluidToFbo(SpriteBatch batch, float fillRatio) {
        soulFbo.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        batch.setProjectionMatrix(fboCamera.combined);
        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (animations.soulFluidIdle != null) {
            TextureRegion fluidFrame = animations.soulFluidIdle.getKeyFrame(soulFluidTime, true);
            float fluidH = fluidFrame.getRegionHeight();
            float emptyY = -fluidH + 18f;
            float fullY = fboHeight - fluidH + 3f;
            float fluidY = MathUtils.lerp(emptyY, fullY, fillRatio);
            batch.draw(fluidFrame, 0f, fluidY);
        }

        if (animations.soulOrbShape != null) {
            batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_ALPHA);
            batch.draw(animations.soulOrbShape, 0f, 0f);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        batch.end();
        soulFbo.end();
    }

    private void drawRegion(SpriteBatch batch, TextureRegion region, float x, float y) {
        if (region == null) {
            return;
        }
        float w = region.getRegionWidth() * HUD_SCALE;
        float h = region.getRegionHeight() * HUD_SCALE;
        batch.draw(region, x, y, w, h);
    }

    private void drawAnimation(SpriteBatch batch, Animation<TextureRegion> animation, float time,
                               float x, float y) {
        if (animation == null) {
            return;
        }
        TextureRegion frame = animation.getKeyFrame(time, false);
        drawRegion(batch, frame, x, y);
    }

    public void dispose() {
        if (soulFbo != null) {
            soulFbo.dispose();
            soulFbo = null;
        }
        if (animations != null) {
            animations.dispose();
        }
    }
}
