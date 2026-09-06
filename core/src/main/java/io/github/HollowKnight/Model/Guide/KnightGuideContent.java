package io.github.HollowKnight.Model.Guide;

import io.github.HollowKnight.Model.SoulVessel;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;
import io.github.HollowKnight.Model.charm.CharmSlots;
import io.github.HollowKnight.Model.entity.Knight;

import java.util.List;

public final class KnightGuideContent {

    public record GuideEntry(String title, String description) {}

    public record GuideSection(String title, List<GuideEntry> entries) {}

    private KnightGuideContent() {}

    public static List<GuideSection> getAbilitySections() {
        return getAbilitySections(GameSettings.Language.ENGLISH);
    }

    public static List<GuideSection> getAbilitySections(GameSettings.Language lang) {
        return List.of(
            section(lang, "guide.sec.movement",
                entry(lang, "guide.walk"),
                entry(lang, "guide.jump"),
                entry(lang, "guide.dash", Knight.DASH_COOLDOWN_BASE)
            ),
            section(lang, "guide.sec.combat",
                entry(lang, "guide.nail", Knight.BASE_NAIL_DAMAGE),
                entry(lang, "guide.knockback", Knight.INVINCIBILITY_DURATION)
            ),
            section(lang, "guide.sec.soul",
                entry(lang, "guide.soulGen", SoulVessel.GAIN_PER_HIT),
                entry(lang, "guide.vessel", SoulVessel.MAX_SOULS),
                entry(lang, "guide.spend")
            ),
            section(lang, "guide.sec.health",
                entry(lang, "guide.masks", Knight.DEFAULT_MAX_MASKS),
                entry(lang, "guide.focus", SoulVessel.FOCUS_COST)
            ),
            section(lang, "guide.sec.spells",
                entry(lang, "guide.casting"),
                entry(lang, "guide.spellCost", SoulVessel.SPELL_COST)
            ),
            section(lang, "guide.sec.charms",
                entry(lang, "guide.charmInv"),
                entry(lang, "guide.notches", CharmSlots.MAX_NOTCHES),
                entry(lang, "guide.passive")
            ),
            section(lang, "guide.sec.bosses",
                entry(lang, "guide.patterns"),
                entry(lang, "guide.dodge"),
                entry(lang, "guide.timing")
            )
        );
    }

    private static GuideSection section(GameSettings.Language lang, String titleKey, GuideEntry... entries) {
        return new GuideSection(SettingsLocalization.get(titleKey, lang), List.of(entries));
    }

    private static GuideEntry entry(GameSettings.Language lang, String keyPrefix, Object... args) {
        String title = SettingsLocalization.get(keyPrefix + ".title", lang);
        String description = args.length == 0
            ? SettingsLocalization.get(keyPrefix + ".desc", lang)
            : SettingsLocalization.format(keyPrefix + ".desc", lang, args);
        return new GuideEntry(title, description);
    }
}
