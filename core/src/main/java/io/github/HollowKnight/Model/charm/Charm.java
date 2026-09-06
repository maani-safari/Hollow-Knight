package io.github.HollowKnight.Model.charm;

public interface Charm {
    String getId();
    String getDisplayName();
    String getDescription();
    String getAddress();
    int    getNotchCost();
    void   onEquip(CharmState ctx);
    void   onUnequip(CharmState ctx);
}
