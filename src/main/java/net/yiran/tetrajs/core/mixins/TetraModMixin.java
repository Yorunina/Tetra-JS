package net.yiran.tetrajs.core.mixins;

import net.yiran.tetrajs.TetraJS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.TetraMod;

@Mixin(value = TetraMod.class, remap = false)
public class TetraModMixin {
    @Inject(method = "<init>",at = @At("RETURN"))
    private void init(CallbackInfo ci){
        TetraJS.registerRequirements();
    }
}
