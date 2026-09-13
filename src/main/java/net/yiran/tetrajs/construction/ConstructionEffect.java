package net.yiran.tetrajs.construction;

import se.mickelus.tetra.effect.ItemEffect;

public final class ConstructionEffect {
    public static final String KEY = "kubejs:construction";
    public static final ItemEffect EFFECT = ItemEffect.get(KEY);

    private ConstructionEffect() {
    }

    public static int maxBlocks(int level) {
        if (level <= 0) {
            return 0;
        }
        return 3 * level * level;
    }
}
