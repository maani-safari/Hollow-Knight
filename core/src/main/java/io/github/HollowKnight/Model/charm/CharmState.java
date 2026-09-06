package io.github.HollowKnight.Model.charm;

public class CharmState {
    public float attackSpeedMultiplier  = 1.0f;
    public float nailDamageMultiplier   = 1.0f;
    public float spellDamageMultiplier  = 1.0f;
    public float knockbackMultiplier    = 1.0f;
    public float incomingDamageMultiplier = 1.0f;

    public int   bonusSoulPerHit        = 0;

    public float dashCooldownMultiplier = 1.0f;
    public float dashDistanceMultiplier = 1.0f;
    public boolean sharpShadowEnabled   = false;

    public float focusSpeedMultiplier   = 1.0f;

    public boolean voidVariantEnabled   = false;

    public void reset() {
        attackSpeedMultiplier   = 1.0f;
        nailDamageMultiplier    = 1.0f;
        spellDamageMultiplier   = 1.0f;
        knockbackMultiplier     = 1.0f;
        incomingDamageMultiplier = 1.0f;
        bonusSoulPerHit         = 0;
        dashCooldownMultiplier  = 1.0f;
        dashDistanceMultiplier  = 1.0f;
        sharpShadowEnabled      = false;
        focusSpeedMultiplier    = 1.0f;
        voidVariantEnabled      = false;
    }
}
