package io.github.HollowKnight.Model.Cheat;

import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CheatCatalog {

    public record CheatEntry(String name, String shortcut, String description) {}

    private record CheatDefinition(String nameKey, String descKey, Function<KeyController, Integer> keyGetter) {}

    private static final List<CheatDefinition> DEFINITIONS = List.of(
        new CheatDefinition("cheat.revive.name", "cheat.revive.desc", KeyController::getEmergency),
        new CheatDefinition("cheat.boss.name", "cheat.boss.desc", KeyController::getBossTeleport),
        new CheatDefinition("cheat.godOn.name", "cheat.godOn.desc", KeyController::getGodStart),
        new CheatDefinition("cheat.godOff.name", "cheat.godOff.desc", KeyController::getGodExit),
        new CheatDefinition("cheat.soul.name", "cheat.soul.desc", KeyController::getHesoyam),
        new CheatDefinition("cheat.noclipOn.name", "cheat.noclipOn.desc", KeyController::getNoclipStart),
        new CheatDefinition("cheat.noclipOff.name", "cheat.noclipOff.desc", KeyController::getNoclipExit)
    );

    private CheatCatalog() {}

    public static List<CheatEntry> getEntries(KeyController keys) {
        return getEntries(keys, GameSettings.Language.ENGLISH);
    }

    public static List<CheatEntry> getEntries(KeyController keys, GameSettings.Language language) {
        String modifier = KeyController.formatKey(keys.getCheatMode());
        List<CheatEntry> entries = new ArrayList<>(DEFINITIONS.size());
        for (CheatDefinition def : DEFINITIONS) {
            String key = KeyController.formatKey(def.keyGetter().apply(keys));
            entries.add(new CheatEntry(
                SettingsLocalization.get(def.nameKey(), language),
                modifier + " + " + key,
                SettingsLocalization.get(def.descKey(), language)
            ));
        }
        return entries;
    }
}
