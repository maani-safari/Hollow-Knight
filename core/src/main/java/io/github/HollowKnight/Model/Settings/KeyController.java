package io.github.HollowKnight.Model.Settings;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

import java.util.List;

public class KeyController {

    public enum BindingId {
        MOVE_LEFT,
        MOVE_RIGHT,
        JUMP,
        DASH,
        ATTACK,
        FOCUS,
        SPELL,
        INVENTORY,
        PAUSE,
        INTERACT
    }

    public record GameplayBinding(String label, String keyDisplay) {}

    public record RebindableBinding(BindingId id, String label) {}

    public static String formatKey(int keyCode) {
        return Input.Keys.toString(keyCode);
    }

    public List<GameplayBinding> getGameplayBindings() {
        return List.of(
            new GameplayBinding("Move Left", formatKey(moveLeft)),
            new GameplayBinding("Move Right", formatKey(moveRight)),
            new GameplayBinding("Jump", formatKey(jump)),
            new GameplayBinding("Dash", formatKey(dash)),
            new GameplayBinding("Attack (Nail)", formatKey(attack)),
            new GameplayBinding("Focus (Heal)", formatKey(focus)),
            new GameplayBinding("Cast Spell", formatKey(vengeful) + " / " + formatKey(howling)),
            new GameplayBinding("Inventory", formatKey(inventory)),
            new GameplayBinding("Interact", formatKey(interact)),
            new GameplayBinding("Pause", formatKey(pause))
        );
    }

    public List<RebindableBinding> getRebindableBindings() {
        return List.of(
            new RebindableBinding(BindingId.MOVE_LEFT, "Move Left"),
            new RebindableBinding(BindingId.MOVE_RIGHT, "Move Right"),
            new RebindableBinding(BindingId.JUMP, "Jump"),
            new RebindableBinding(BindingId.DASH, "Dash"),
            new RebindableBinding(BindingId.ATTACK, "Attack"),
            new RebindableBinding(BindingId.FOCUS, "Focus"),
            new RebindableBinding(BindingId.SPELL, "Spell"),
            new RebindableBinding(BindingId.INVENTORY, "Inventory"),
            new RebindableBinding(BindingId.INTERACT, "Interact"),
            new RebindableBinding(BindingId.PAUSE, "Pause")
        );
    }

    public int getBindingKey(BindingId id) {
        return switch (id) {
            case MOVE_LEFT -> moveLeft;
            case MOVE_RIGHT -> moveRight;
            case JUMP -> jump;
            case DASH -> dash;
            case ATTACK -> attack;
            case FOCUS -> focus;
            case SPELL -> vengeful;
            case INVENTORY -> inventory;
            case INTERACT -> interact;
            case PAUSE -> pause;
        };
    }

    public void setBindingKey(BindingId id, int keyCode) {
        switch (id) {
            case MOVE_LEFT -> moveLeft = keyCode;
            case MOVE_RIGHT -> moveRight = keyCode;
            case JUMP -> jump = keyCode;
            case DASH -> dash = keyCode;
            case ATTACK -> attack = keyCode;
            case FOCUS -> focus = keyCode;
            case SPELL -> vengeful = keyCode;
            case INVENTORY -> inventory = keyCode;
            case INTERACT -> interact = keyCode;
            case PAUSE -> pause = keyCode;
        }
    }

    public void loadFromPreferences(Preferences prefs) {
        moveLeft = prefs.getInteger("key_moveLeft", moveLeft);
        moveRight = prefs.getInteger("key_moveRight", moveRight);
        jump = prefs.getInteger("key_jump", jump);
        dash = prefs.getInteger("key_dash", dash);
        attack = prefs.getInteger("key_attack", attack);
        focus = prefs.getInteger("key_focus", focus);
        vengeful = prefs.getInteger("key_spell", vengeful);
        inventory = prefs.getInteger("key_inventory", inventory);
        interact = prefs.getInteger("key_interact", interact);
        pause = prefs.getInteger("key_pause", pause);
    }

    public void saveToPreferences(Preferences prefs) {
        prefs.putInteger("key_moveLeft", moveLeft);
        prefs.putInteger("key_moveRight", moveRight);
        prefs.putInteger("key_jump", jump);
        prefs.putInteger("key_dash", dash);
        prefs.putInteger("key_attack", attack);
        prefs.putInteger("key_focus", focus);
        prefs.putInteger("key_spell", vengeful);
        prefs.putInteger("key_inventory", inventory);
        prefs.putInteger("key_interact", interact);
        prefs.putInteger("key_pause", pause);
        prefs.flush();
    }
    private int moveLeft = Input.Keys.LEFT;
    private int moveRight = Input.Keys.RIGHT;
    private int moveUp = Input.Keys.UP;
    private int moveDown = Input.Keys.DOWN;
    private int jump = Input.Keys.Z;
    private int attack = Input.Keys.X;
    private int dash = Input.Keys.C;
    private int focus = Input.Keys.A;
    private int inventory = Input.Keys.I;
    private int interact = Input.Keys.E;
    private int pause = Input.Keys.ESCAPE;
    private int vengeful = Input.Keys.F;
    private int howling = Input.Keys.V;
    private int cheatMode = Input.Keys.CONTROL_LEFT;
    private int noclipStart = Input.Keys.Q;
    private int noclipExit = Input.Keys.E;
    private int godStart= Input.Keys.G;
    private int godExit = Input.Keys.H;
    private int hesoyam = Input.Keys.S;
    private int emergency = Input.Keys.CAPS_LOCK;
    private int BossTeleport = Input.Keys.B;
    private static final KeyController defaultKey = new KeyController();

    public void resetGameplayBindings() {
        moveLeft = defaultKey.moveLeft;
        moveRight = defaultKey.moveRight;
        jump = defaultKey.jump;
        dash = defaultKey.dash;
        attack = defaultKey.attack;
        focus = defaultKey.focus;
        vengeful = defaultKey.vengeful;
        inventory = defaultKey.inventory;
        interact = defaultKey.interact;
        pause = defaultKey.pause;
    }

    public void resetToDefault(){

         moveLeft = defaultKey.moveLeft;
         moveRight = defaultKey.moveRight;
         moveUp = defaultKey.moveUp ;
         moveDown = defaultKey.moveDown;
         jump =defaultKey.jump;
         attack = defaultKey.attack;
         dash = defaultKey.dash;
         focus =defaultKey.focus;
         inventory =defaultKey.inventory;
         interact = defaultKey.interact;
         pause = defaultKey.pause;
         vengeful = defaultKey.vengeful;
         howling = defaultKey.howling;
         cheatMode = defaultKey.cheatMode;
        noclipStart = defaultKey.noclipStart;
        noclipExit = defaultKey.noclipExit;
        godStart = defaultKey.godStart;
        godExit = defaultKey.godExit;
        hesoyam = defaultKey.hesoyam;
        emergency = defaultKey.emergency;
        BossTeleport = defaultKey.BossTeleport;

    }

    public int getInteract() {
        return interact;
    }

    public void setInteract(int interact) {
        this.interact = interact;
    }

    public int getPause() {
        return pause;
    }

    public void setPause(int pause) {
        this.pause = pause;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public int getFocus() {
        return focus;
    }

    public void setFocus(int focus) {
        this.focus = focus;
    }

    public int getDash() {
        return dash;
    }

    public void setDash(int dash) {
        this.dash = dash;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getJump() {
        return jump;
    }

    public void setJump(int jump) {
        this.jump = jump;
    }

    public int getMoveDown() {
        return moveDown;
    }

    public void setMoveDown(int moveDown) {
        this.moveDown = moveDown;
    }

    public int getMoveUp() {
        return moveUp;
    }

    public void setMoveUp(int moveUp) {
        this.moveUp = moveUp;
    }

    public int getMoveRight() {
        return moveRight;
    }

    public void setMoveRight(int moveRight) {
        this.moveRight = moveRight;
    }

    public int getMoveLeft() {
        return moveLeft;
    }

    public void setMoveLeft(int moveLeft) {
        this.moveLeft = moveLeft;
    }

    public int getVengeful() {
        return vengeful;
    }

    public void setVengeful(int vengeful) {
        this.vengeful = vengeful;
    }

    public int getHowling() {
        return howling;
    }

    public void setHowling(int howling) {
        this.howling = howling;
    }

    public int getNoclipStart() {
        return noclipStart;
    }

    public void setNoclipStart(int noclipStart) {
        this.noclipStart = noclipStart;
    }

    public int getCheatMode() {
        return cheatMode;
    }

    public void setCheatMode(int cheatMode) {
        this.cheatMode = cheatMode;
    }

    public int getNoclipExit() {
        return noclipExit;
    }

    public void setNoclipExit(int noclipExit) {
        this.noclipExit = noclipExit;
    }

    public int getGodStart() {
        return godStart;
    }

    public void setGodStart(int godStart) {
        this.godStart = godStart;
    }

    public int getGodExit() {
        return godExit;
    }

    public void setGodExit(int godExit) {
        this.godExit = godExit;
    }

    public int getHesoyam() {
        return hesoyam;
    }

    public void setHesoyam(int hesoyam) {
        this.hesoyam = hesoyam;
    }

    public int getEmergency() {
        return emergency;
    }

    public void setEmergency(int emergency) {
        this.emergency = emergency;
    }

    public int getBossTeleport() {
        return BossTeleport;
    }

    public void setBossTeleport(int bossTeleport) {
        BossTeleport = bossTeleport;
    }
}
