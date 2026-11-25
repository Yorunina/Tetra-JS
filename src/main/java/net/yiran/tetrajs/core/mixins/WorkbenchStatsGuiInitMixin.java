package net.yiran.tetrajs.core.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.yiran.tetrajs.api.StatBarInjector;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;

@Mixin(WorkbenchStatsGui.class)
public abstract class WorkbenchStatsGuiInitMixin {

    @Inject(method = "<init>(II)V", at = @At("TAIL"), remap = false)
    private void tetrajs$applyPendingIndicators(int x, int y, CallbackInfo ci) {
        StatBarInjector.applyPending();
    }
}
