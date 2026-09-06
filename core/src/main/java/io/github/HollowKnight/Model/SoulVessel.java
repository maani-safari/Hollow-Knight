package io.github.HollowKnight.Model;

public class SoulVessel {
    public static final int MAX_SOULS = 99;
    public static final int GAIN_PER_HIT = 11;
    public static final int SPELL_COST = 33;
    public static final int FOCUS_COST = 33;
    private int currentSouls;
    public SoulVessel(int currentSouls) {
        this.currentSouls = Math.min(currentSouls , MAX_SOULS);
    }
    public int gain(int amount){
        int before = currentSouls;
        currentSouls = Math.min(MAX_SOULS , amount + before );
        return currentSouls - before;
    }
    public boolean consume(int amount){
        if (currentSouls < amount)
            return false;
        currentSouls -= amount;
        return true;
    }
    public boolean canSpell(){
        return currentSouls >= SPELL_COST;
    }
    public boolean canFocus(){
        return currentSouls >= FOCUS_COST;
    }
    public float fillRatio(){
        return (float) currentSouls / MAX_SOULS;
    }
    public void  reset(){
        currentSouls = 0;
    }
    public void fillMax(){
        currentSouls = MAX_SOULS;
    }
    public int getCurrentSouls() {
        return currentSouls;
    }
    public void setCurrentSouls(int currentSouls) {
        this.currentSouls = currentSouls;
    }

}
