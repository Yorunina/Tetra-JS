package net.yiran.tetrajs.core.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;

@Mixin(GuiStatBar.class)
public interface GuiStatBarAccessor {

    @Accessor(value = "labelKey", remap = false)
    String tetrajs$getLabelKey();

    @Accessor(value = "indicators", remap = false)
    GuiStatIndicator[] tetrajs$getIndicators();
}
