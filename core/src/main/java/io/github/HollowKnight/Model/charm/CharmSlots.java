package io.github.HollowKnight.Model.charm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CharmSlots {
    public static final int MAX_NOTCHES = 3;

    private final List<Charm> equipped = new ArrayList<>();

    public boolean equip(Charm charm) {
        if (equipped.contains(charm)) return false;
        if (usedNotches() + charm.getNotchCost() > MAX_NOTCHES) return false;
        equipped.add(charm);
        return true;
    }

    public boolean unequip(Charm charm) {
        return equipped.remove(charm);
    }

    public boolean isEquipped(Charm charm)     { return equipped.contains(charm); }
    public int usedNotches() {
        int total = 0;
        for (Charm charm : equipped) {
            total += charm.getNotchCost();
        }
        return total;
    }

    public int freeNotches()                   { return MAX_NOTCHES - usedNotches(); }
    public boolean isFull()                    { return freeNotches() <= 0; }
    public List<Charm> getEquipped()           { return Collections.unmodifiableList(equipped); }

    public void clear()                        { equipped.clear(); }
}
