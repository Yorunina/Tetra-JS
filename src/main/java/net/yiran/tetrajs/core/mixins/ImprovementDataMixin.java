package net.yiran.tetrajs.core.mixins;

import net.yiran.tetrajs.core.InfiniteImprovementAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import se.mickelus.tetra.module.data.ImprovementData;

@Mixin(value = ImprovementData.class, remap = false)
public class ImprovementDataMixin implements InfiniteImprovementAccess {
    @Unique
    public boolean infinite;

    @Override
    public boolean tetrajs$isInfinite() {
        return this.infinite;
    }

    @Override
    public void tetrajs$setInfinite(boolean infinite) {
        this.infinite = infinite;
    }
}