package net.yiran.tetrajs.core.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.blocks.scroll.ScrollData;

@Mixin(value = ScrollData.class, remap = false)
public class ScrollDataMixin {
    @Inject(method = "lambda$readRibbonFast$17", at = @At("HEAD"), cancellable = true)
    private static void tetrajs$skipEmptyRibbon(String hex, CallbackInfoReturnable<Integer> cir) {
        if (hex == null || hex.isEmpty()) {
            cir.setReturnValue(0);
        }
    }
}