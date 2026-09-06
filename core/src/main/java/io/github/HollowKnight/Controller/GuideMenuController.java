package io.github.HollowKnight.Controller;

import io.github.HollowKnight.Main;
import io.github.HollowKnight.Model.Cheat.CheatCatalog;
import io.github.HollowKnight.Model.Cheat.CheatCatalog.CheatEntry;
import io.github.HollowKnight.Model.Guide.KnightGuideContent;
import io.github.HollowKnight.Model.Guide.KnightGuideContent.GuideSection;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.Settings.KeyController.GameplayBinding;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;
import io.github.HollowKnight.View.MainMenuScreen;

import java.util.ArrayList;
import java.util.List;

public class GuideMenuController {

    private final Main main;
    private final KeyController keys;

    public GuideMenuController(Main main, KeyController keys) {
        this.main = main;
        this.keys = keys;
    }

    private GameSettings.Language lang() {
        return main.getGameSettings().getLanguage();
    }

    public List<GameplayBinding> getControlBindings() {
        GameSettings.Language language = lang();
        List<GameplayBinding> localized = new ArrayList<>();
        for (GameplayBinding binding : keys.getGameplayBindings()) {
            String labelKey = switch (binding.label()) {
                case "Move Left" -> "bind.MOVE_LEFT";
                case "Move Right" -> "bind.MOVE_RIGHT";
                case "Jump" -> "bind.JUMP";
                case "Dash" -> "bind.DASH";
                case "Attack (Nail)" -> "bind.ATTACK_NAIL";
                case "Focus (Heal)" -> "bind.FOCUS_HEAL";
                case "Cast Spell" -> "bind.CAST_SPELL";
                case "Inventory" -> "bind.INVENTORY";
                case "Interact" -> "bind.INTERACT";
                case "Pause" -> "bind.PAUSE";
                default -> null;
            };
            String label = labelKey == null ? binding.label() : SettingsLocalization.get(labelKey, language);
            localized.add(new GameplayBinding(label, binding.keyDisplay()));
        }
        return localized;
    }

    public List<GuideSection> getAbilitySections() {
        return KnightGuideContent.getAbilitySections(lang());
    }

    public List<CheatEntry> getCheatEntries() {
        return CheatCatalog.getEntries(keys, lang());
    }

    public void close() {
        main.setScreen(new MainMenuScreen(main.getSkin(), main));
    }
}
