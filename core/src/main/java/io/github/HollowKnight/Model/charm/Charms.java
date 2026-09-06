package io.github.HollowKnight.Model.charm;

public enum Charms implements Charm {
    HEAVY_BLOW(
        "heavy_blow",
        "Heavy Blow",
        "Increases the knockback of Nail attacks.",
        1,
        "Charms/Heavy Blow - _0008_charm_nail_damage_up.png"
    ) {
        @Override
        void apply(CharmState ctx) {
            ctx.knockbackMultiplier *= 1.5f;
        }
    },
    QUICK_SLASH(
        "quick_slash",
        "Quick Slash",
        "Nail attacks come out faster.",
        1,
        "Charms/Quick Slash - _0003_charm_nail_slash_speed_up.png"
    ) {
        @Override
        void apply(CharmState ctx) {
            ctx.attackSpeedMultiplier *= 1.3f;
        }
    },
    SOUL_CATCHER(
        "soul_catcher",
        "Soul Catcher",
        "Gain more soul when striking foes.",
        1,
        "Charms/Soul Catcher - _0001_charm_more_soul.png"
    ) {
        @Override
        void apply(CharmState ctx) {
            ctx.bonusSoulPerHit += 5;
        }
    },
    UNBREAKABLE_STRENGTH(
        "unbreakable_strength",
        "Unbreakable\nStrength",
        "Increases the damage of Nail strikes.",
        1,
        "Charms/Unbreakable Strength_0002_charm_glass_attack_up_full.png"
    ) {
        @Override
        void apply(CharmState ctx) {
            ctx.nailDamageMultiplier *= 1.4f;
        }
    },
    QUICK_FOCUS(
        "quick-focus",
        "Quick Focus",
        "Focus heals faster.",
        1,
        "Charms/Quick Focus - _0005_charm_fast_focus.png"

    ) {
        @Override
        void apply(CharmState ctx) {
            ctx.focusSpeedMultiplier *= 1.5f;
        }
    },
//    SHARP_SHADOW(
//        "sharp_shadow",
//        "Sharp Shadow",
//        "Dash travels farther.",
//        1,
//        "Sharp Shadow - charm_shade_impact"
//    ) {
//        @Override
//        void apply(CharmState ctx) {
//            ctx.dashDistanceMultiplier *= 1.35f;
//        }
//    },
    DASHMASTER(
        "dashmaster",
        "Dashmaster",
        "Dash travels farther.",
        1,
        "Charms/Dashmaster - _0011_charm_generic_03.png"
    ) {
        @Override
        void apply(CharmState ctx) {
            ctx.dashDistanceMultiplier *= 1.35f;
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final int notchCost;
    private final String pngAd;

    Charms(String id, String displayName, String description, int notchCost , String pngAd) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.notchCost = notchCost;
        this.pngAd  = pngAd;
    }

    abstract void apply(CharmState ctx);

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getAddress() {
        return pngAd;
    }

    @Override
    public int getNotchCost() {
        return notchCost;
    }

    @Override
    public void onEquip(CharmState ctx) {
        apply(ctx);
    }

    @Override
    public void onUnequip(CharmState ctx) {
    }

    /** Resolves a charm from its persisted id, or returns {@code null} when unknown. */
    public static Charms fromId(String id) {
        if (id == null) {
            return null;
        }
        for (Charms charm : values()) {
            if (charm.getId().equals(id)) {
                return charm;
            }
        }
        return null;
    }
}
