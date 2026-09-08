package net.yiran.tetrajs.core.mixins;

import net.yiran.tetrajs.core.InfiniteImprovementAccess;
import net.yiran.tetrajs.core.InfiniteImprovements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.MaterialData;
import se.mickelus.tetra.module.data.MaterialImprovementData;

@Mixin(value = MaterialImprovementData.class, remap = false)
public class MaterialImprovementDataMixin {
    @Inject(method = "combine", at = @At("RETURN"))
    private void tetrajs$copyInfinite(MaterialData material, CallbackInfoReturnable<ImprovementData> cir) {
        if (!InfiniteImprovements.isInfinite((ImprovementData) (Object) this)) {
            return;
        }
        ImprovementData result = cir.getReturnValue();
        if (result instanceof InfiniteImprovementAccess access) {
            access.tetrajs$setInfinite(true);
        }
    }
}