package io.github.HollowKnight.Model.charm;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class CharmInventory {
    private final Set<Charms> owned = EnumSet.noneOf(Charms.class);

    public void unlock(Charms charm) {
        owned.add(charm);
    }

    public void unlockAll() {
        owned.addAll(EnumSet.allOf(Charms.class));
    }

    public boolean owns(Charms charm) {
        return owned.contains(charm);
    }

    public Set<Charms> getOwned() {
        return Collections.unmodifiableSet(owned);
    }

    /** Replaces owned charms with the given collection (used when loading a save). */
    public void restoreOwned(Iterable<Charms> charms) {
        owned.clear();
        for (Charms charm : charms) {
            owned.add(charm);
        }
    }
}
