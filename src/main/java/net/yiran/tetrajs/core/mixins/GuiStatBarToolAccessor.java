package net.yiran.tetrajs.core.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import se.mickelus.tetra.blocks.workbench.gui.GuiTool;
import se.mickelus.tetra.gui.stats.bar.GuiStatBarTool;

@Mixin(GuiStatBarTool.class)
public interface GuiStatBarToolAccessor {

    @Accessor(value = "icon", remap = false)
    GuiTool tetrajs$getIcon();
}
