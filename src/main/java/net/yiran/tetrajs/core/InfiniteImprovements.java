package net.yiran.tetrajs.core;

import se.mickelus.tetra.module.data.ImprovementData;

public final class InfiniteImprovements {
    private InfiniteImprovements() {
    }

    public static boolean isInfinite(ImprovementData data) {
        return data instanceof InfiniteImprovementAccess access && access.tetrajs$isInfinite();
    }
}
