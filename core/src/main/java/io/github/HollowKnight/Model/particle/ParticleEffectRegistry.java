package io.github.HollowKnight.Model.particle;

import java.util.HashMap;
import java.util.Map;


public  class ParticleEffectRegistry {

    private static final Map<String, ParticleEffect> EFFECTS = new HashMap<>();

    static {
        register("floating_dust", new FloatingDustEffect());
        register("greenpath_leaves", new GreenpathLeafEffect());
    }

    private ParticleEffectRegistry() {
    }

    public static void register(String id, ParticleEffect effect) {
        EFFECTS.put(id, effect);
    }

    public static ParticleEffect get(String id) {
        return EFFECTS.get(id);
    }

    public static boolean has(String id) {
        return EFFECTS.containsKey(id);
    }
}
