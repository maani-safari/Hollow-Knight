package io.github.HollowKnight.Model.Settings;

import java.util.HashMap;
import java.util.Map;

/**
 * UI string table for English and French.
 */
public final class SettingsLocalization {

    private SettingsLocalization() {}

    public static String get(String key, GameSettings.Language language) {
        Map<GameSettings.Language, String> translations = STRINGS.get(key);
        if (translations == null) {
            return key;
        }
        String value = translations.get(language);
        if (value != null) {
            return value;
        }
        return translations.getOrDefault(GameSettings.Language.ENGLISH, key);
    }

    public static String format(String key, GameSettings.Language language, Object... args) {
        return String.format(java.util.Locale.ROOT, get(key, language), args);
    }

    private static Map<GameSettings.Language, String> t(String en, String fr) {
        Map<GameSettings.Language, String> map = new HashMap<>(2);
        map.put(GameSettings.Language.ENGLISH, en);
        map.put(GameSettings.Language.FRENCH, fr);
        return map;
    }

    private static final Map<String, Map<GameSettings.Language, String>> STRINGS = new HashMap<>();

    static {
        // Settings
        put("title", "OPTIONS", "OPTIONS");
        put("audio", "AUDIO", "AUDIO");
        put("musicVolume", "Music Volume", "Volume de la musique");
        put("musicEnabled", "Music", "Musique");
        put("sfxEnabled", "Sound Effects", "Effets sonores");
        put("resetAudio", "Reset Audio", "Réinitialiser l'audio");
        put("display", "DISPLAY", "AFFICHAGE");
        put("brightness", "Brightness", "Luminosité");
        put("language", "Language", "Langue");
        put("controls", "CONTROLS", "COMMANDES");
        put("resetControls", "Reset Controls", "Réinitialiser les commandes");
        put("back", "BACK", "RETOUR");
        put("pressKey", "Press a key...", "Appuyez sur une touche...");
        put("langEnglish", "English", "Anglais");
        put("langFrench", "French", "Français");

        // Main menu
        put("main.start", "START GAME", "NOUVELLE PARTIE");
        put("main.load", "LOAD GAME", "CHARGER");
        put("main.options", "OPTIONS", "OPTIONS");
        put("main.guide", "GUIDE", "GUIDE");
        put("main.achievements", "ACHIEVEMENTS", "SUCCÈS");
        put("main.quit", "QUIT GAME", "QUITTER");

        // Profile
        put("profile.title", "SELECT PROFILE", "CHOISIR UN PROFIL");
        put("profile.newGame", "NEW GAME", "NOUVELLE PARTIE");
        put("profile.corrupted", "CORRUPTED SAVE", "SAUVEGARDE CORROMPUE");
        put("profile.continue", "CONTINUE — %s — %s", "CONTINUER — %s — %s");

        // Pause
        put("pause.title", "PAUSED", "PAUSE");
        put("pause.resume", "RESUME", "REPRENDRE");
        put("pause.save", "SAVE GAME", "SAUVEGARDER");
        put("pause.options", "OPTIONS", "OPTIONS");
        put("pause.mainMenu", "MAIN MENU", "MENU PRINCIPAL");
        put("pause.quickHint", "F5 Quick Save  |  F9 Quick Load", "F5 Sauvegarde rapide  |  F9 Chargement rapide");
        put("pause.cheatsHeader", "CHEAT CODES", "CODES DE TRICHE");
        put("pause.saved", "Game saved.", "Partie sauvegardée.");
        put("pause.saveFailed", "Save failed.", "Échec de la sauvegarde.");
        put("pause.noSave", "No valid save found.", "Aucune sauvegarde valide.");
        put("pause.loaded", "Game loaded.", "Partie chargée.");
        put("save.statusSlot", "Game saved to slot %d.", "Partie sauvegardée dans l'emplacement %d.");

        // Guide
        put("guide.title", "GUIDE", "GUIDE");
        put("guide.controls", "GAME CONTROLS", "COMMANDES");
        put("guide.abilities", "KNIGHT ABILITIES", "CAPACITÉS DU CHEVALIER");
        put("guide.cheats", "CHEAT CODES", "CODES DE TRICHE");
        put("guide.shortcut", "  Shortcut: %s", "  Raccourci : %s");

        // Control / rebind labels
        put("bind.MOVE_LEFT", "Move Left", "Aller à gauche");
        put("bind.MOVE_RIGHT", "Move Right", "Aller à droite");
        put("bind.JUMP", "Jump", "Sauter");
        put("bind.DASH", "Dash", "Dash");
        put("bind.ATTACK", "Attack", "Attaquer");
        put("bind.ATTACK_NAIL", "Attack (Nail)", "Attaquer (Clou)");
        put("bind.FOCUS", "Focus", "Focus");
        put("bind.FOCUS_HEAL", "Focus (Heal)", "Focus (Soin)");
        put("bind.SPELL", "Spell", "Sort");
        put("bind.CAST_SPELL", "Cast Spell", "Lancer un sort");
        put("bind.INVENTORY", "Inventory", "Inventaire");
        put("bind.INTERACT", "Interact", "Interagir");
        put("bind.PAUSE", "Pause", "Pause");

        // Achievements UI
        put("achievements.title", "ACHIEVEMENTS", "SUCCÈS");
        put("achievements.unlocked", "Unlocked", "Débloqué");
        put("achievements.locked", "Locked", "Verrouillé");
        put("achievements.popup", "Achievement Unlocked!", "Succès débloqué !");

        put("ach.completion.title", "Path of Completion", "Voie de l'accomplissement");
        put("ach.completion.desc", "Finish the game.", "Terminez le jeu.");
        put("ach.speedrun.title", "Speedrunner", "Speedrunner");
        put("ach.speedrun.desc", "Finish the game within the time limit.", "Terminez le jeu dans le temps imparti.");
        put("ach.true_hunter.title", "True Hunter", "Véritable chasseur");
        put("ach.true_hunter.desc", "Defeat every enemy type at least once.", "Vainquez chaque type d'ennemi au moins une fois.");
        put("ach.defeat_false_knight.title", "False Knight", "Faux Chevalier");
        put("ach.defeat_false_knight.desc", "Defeat the False Knight.", "Vainquez le Faux Chevalier.");
        put("ach.explorer.title", "Explorer", "Explorateur");
        put("ach.explorer.desc", "Enter every room in the kingdom.", "Entrez dans chaque salle du royaume.");

        // Charms UI
        put("charms.title", "CHARMS", "CHARMES");
        put("charms.owned", "Owned", "Possédés");
        put("charms.equipped", "Equipped", "Équipés");
        put("charms.select", "Select a charm.", "Sélectionnez un charme.");
        put("charms.closeHint", "Press I or Esc to close", "Appuyez sur I ou Échap pour fermer");
        put("charms.notches", "Notches: %d / %d", "Encoches : %d / %d");
        put("charms.unequipped", "Unequipped %s.", "%s déséquipé.");
        put("charms.notEnough", "Not enough notches for %s (%d).", "Pas assez d'encoches pour %s (%d).");

        // Victory
        put("victory.title", "Victory!", "Victoire !");
        put("victory.deaths", "Total Deaths: %d", "Morts totales : %d");
        put("victory.kills", "Total Enemies Killed: %d", "Ennemis vaincus : %d");
        put("victory.time", "Total Play Time: %s", "Temps de jeu : %s");
        put("victory.restart", "RESTART GAME", "RECOMMENCER");
        put("victory.menu", "RETURN TO MAIN MENU", "MENU PRINCIPAL");

        // Interaction
        put("hint.talk", "Press E to Talk", "Appuyez sur E pour parler");

        // Cheats
        put("cheat.revive.name", "Emergency Revive", "Réanimation d'urgence");
        put("cheat.revive.desc", "While dead, instantly revive and restore one health mask.", "Une fois mort, revivez instantanément et récupérez un masque de vie.");
        put("cheat.boss.name", "Boss Teleport", "Téléportation au boss");
        put("cheat.boss.desc", "Teleport to the boss arena entry point in the current room.", "Téléportez-vous à l'entrée de l'arène du boss dans la salle actuelle.");
        put("cheat.godOn.name", "God Mode (Enable)", "Mode dieu (activer)");
        put("cheat.godOn.desc", "Become invincible — take no damage from enemies or hazards.", "Devenez invincible — aucun dégât des ennemis ou des dangers.");
        put("cheat.godOff.name", "God Mode (Disable)", "Mode dieu (désactiver)");
        put("cheat.godOff.desc", "Turn off invincibility and return to normal damage.", "Désactivez l'invincibilité et revenez aux dégâts normaux.");
        put("cheat.soul.name", "Fill Soul Vessel", "Remplir le vaisseau d'âme");
        put("cheat.soul.desc", "Fill the Soul Vessel to maximum capacity.", "Remplissez complètement le vaisseau d'âme.");
        put("cheat.noclipOn.name", "Noclip (Enable)", "Noclip (activer)");
        put("cheat.noclipOn.desc", "Fly freely through walls and terrain.", "Volez librement à travers murs et terrain.");
        put("cheat.noclipOff.name", "Noclip (Disable)", "Noclip (désactiver)");
        put("cheat.noclipOff.desc", "Exit noclip and return to normal physics.", "Quittez le noclip et revenez à la physique normale.");

        // Guide ability sections
        put("guide.sec.movement", "Movement", "Mouvement");
        put("guide.sec.combat", "Combat", "Combat");
        put("guide.sec.soul", "Soul System", "Système d'âme");
        put("guide.sec.health", "Health", "Santé");
        put("guide.sec.spells", "Spells", "Sorts");
        put("guide.sec.charms", "Charms", "Charmes");
        put("guide.sec.bosses", "Bosses", "Boss");

        put("guide.walk.title", "Walk", "Marcher");
        put("guide.walk.desc", "Hold left or right to move across platforms and explore rooms.", "Maintenez gauche ou droite pour vous déplacer et explorer les salles.");
        put("guide.jump.title", "Jump", "Sauter");
        put("guide.jump.desc", "Press the jump key to leap. Release early for a shorter hop.", "Appuyez sur saut pour bondir. Relâchez tôt pour un petit saut.");
        put("guide.dash.title", "Dash", "Dash");
        put("guide.dash.desc", "Press dash for a quick burst of speed. Cooldown lasts %s seconds.", "Appuyez sur dash pour une accélération. Temps de recharge : %s s.");

        put("guide.nail.title", "Nail attacks", "Attaques au Clou");
        put("guide.nail.desc", "Press attack to swing the Nail. Each hit deals %s base damage.", "Appuyez sur attaquer pour frapper. Chaque coup inflige %s de dégâts de base.");
        put("guide.knockback.title", "Knockback", "Recul");
        put("guide.knockback.desc", "Taking damage applies knockback and brief invincibility (%ss).", "Subir des dégâts provoque un recul et une brève invincibilité (%s s).");

        put("guide.soulGen.title", "Generating Soul", "Générer de l'âme");
        put("guide.soulGen.desc", "Hitting enemies with the Nail generates Soul (+%s per hit).", "Frapper les ennemis avec le Clou génère de l'âme (+%s par coup).");
        put("guide.vessel.title", "Soul Vessel", "Vaisseau d'âme");
        put("guide.vessel.desc", "Soul is stored in the Soul Vessel (max %s).", "L'âme est stockée dans le vaisseau d'âme (max %s).");
        put("guide.spend.title", "Spending Soul", "Dépenser l'âme");
        put("guide.spend.desc", "Soul is consumed when healing (Focus) or casting spells.", "L'âme est consommée pour soigner (Focus) ou lancer des sorts.");

        put("guide.masks.title", "Health Masks", "Masques de vie");
        put("guide.masks.desc", "The Knight has up to %s Health Masks. Each mask represents one hit of health.", "Le Chevalier a jusqu'à %s masques de vie. Chaque masque représente un point de vie.");
        put("guide.focus.title", "Focus", "Focus");
        put("guide.focus.desc", "Hold Focus to channel healing. Costs %s Soul to restore one mask.", "Maintenez Focus pour soigner. Coûte %s d'âme pour restaurer un masque.");

        put("guide.casting.title", "Casting", "Incantation");
        put("guide.casting.desc", "Press a spell key to cast. Vengeful Spirit fires a projectile; Howling Wraiths strikes upward while airborne.", "Appuyez sur une touche de sort. Esprit vengeur lance un projectile ; Hurlements frappe vers le haut en l'air.");
        put("guide.spellCost.title", "Soul cost", "Coût en âme");
        put("guide.spellCost.desc", "Each spell costs %s Soul. You need a full spell charge to cast.", "Chaque sort coûte %s d'âme. Il faut une charge complète pour lancer.");

        put("guide.charmInv.title", "Charm inventory", "Inventaire de charmes");
        put("guide.charmInv.desc", "Open the inventory to browse owned charms and read their effects.", "Ouvrez l'inventaire pour parcourir vos charmes et lire leurs effets.");
        put("guide.notches.title", "Notches", "Encoches");
        put("guide.notches.desc", "Charms cost Notches to equip. You have %s Notch slots available.", "Les charmes coûtent des encoches. Vous avez %s emplacements.");
        put("guide.passive.title", "Passive effects", "Effets passifs");
        put("guide.passive.desc", "Equipped charms apply passive bonuses such as extra damage, faster focus, or protection.", "Les charmes équipés donnent des bonus passifs : dégâts, focus plus rapide, protection…");

        put("guide.patterns.title", "Attack patterns", "Schémas d'attaque");
        put("guide.patterns.desc", "Bosses have unique attack patterns — learn each phase carefully.", "Les boss ont des schémas uniques — apprenez chaque phase.");
        put("guide.dodge.title", "Dodge first", "Esquiver d'abord");
        put("guide.dodge.desc", "Dodge or dash before attacking. Rushing in leads to damage.", "Esquivez ou dashez avant d'attaquer. Se précipiter mène aux dégâts.");
        put("guide.timing.title", "Timing", "Timing");
        put("guide.timing.desc", "Watch for attack wind-ups and strike during recovery windows.", "Guettez les préparations d'attaque et frappez pendant la récupération.");
    }

    private static void put(String key, String en, String fr) {
        STRINGS.put(key, t(en, fr));
    }
}
