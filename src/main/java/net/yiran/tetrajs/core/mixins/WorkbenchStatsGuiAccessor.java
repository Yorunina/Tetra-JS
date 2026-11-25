package net.yiran.tetrajs.core.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;

@Mixin(WorkbenchStatsGui.class)
public interface WorkbenchStatsGuiAccessor {

    @Accessor(value = "staticBars", remap = false)
    static List<?> tetrajs$getStaticBars() {
        throw new AssertionError();
    }
}
