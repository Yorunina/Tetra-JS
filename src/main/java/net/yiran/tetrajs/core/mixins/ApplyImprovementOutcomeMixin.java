package net.yiran.tetrajs.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.yiran.tetrajs.core.InfiniteImprovements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import se.mickelus.tetra.craftingeffect.StackMode;
import se.mickelus.tetra.craftingeffect.outcome.ApplyImprovementOutcome;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;

import java.util.Map;

@Mixin(value = ApplyImprovementOutcome.class, remap = false)
public class ApplyImprovementOutcomeMixin {
    @WrapOperation(
            method = "lambda$apply$5",
            at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/craftingeffect/StackMode;evaluate(II)I")
    )
    private int tetrajs$skipStackingDirect(StackMode stacking, int current, int added, Operation<Integer> original,
                                          ItemStack upgradedStack, ItemModuleMajor module, @Local Map.Entry<?, ?> improvement) {
        return tetrajs$skipIfInfinite(stacking, current, added, original, module, upgradedStack, String.valueOf(improvement.getKey()));
    }

    @WrapOperation(
            method = "lambda$apply$2",
            at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/craftingeffect/StackMode;evaluate(II)I")
    )
    private int tetrajs$skipStackingAspect(StackMode stacking, int current, int added, Operation<Integer> original,
                                          ItemModuleMajor module, ItemStack upgradedStack, Map.Entry<?, ?> entry, String improvementKey) {
        return tetrajs$skipIfInfinite(stacking, current, added, original, module, upgradedStack, improvementKey);
    }

    private static int tetrajs$skipIfInfinite(StackMode stacking, int current, int added, Operation<Integer> original,
                                             ItemModuleMajor module, ItemStack upgradedStack, String key) {
        ImprovementData data = module.getImprovement(upgradedStack, key);
        if (InfiniteImprovements.isInfinite(data)) {
            return added;
        }
        return original.call(stacking, current, added);
    }
}