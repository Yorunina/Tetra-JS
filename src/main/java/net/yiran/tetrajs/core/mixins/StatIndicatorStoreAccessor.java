package net.yiran.tetrajs.core.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;
import se.mickelus.tetra.gui.stats.data.StatIndicatorStore;

@Mixin(StatIndicatorStore.class)
public interface StatIndicatorStoreAccessor {

    @Accessor(value = "instance", remap = false)
    static StatIndicatorStore tetrajs$getInstance() {
        throw new AssertionError();
    }

    @Accessor(value = "indicators", remap = false)
    Map<ResourceLocation, GuiStatIndicator> tetrajs$getIndicatorMap();
}
