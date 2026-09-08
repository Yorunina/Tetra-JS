package net.yiran.tetrajs.core.mixins.tetrawear;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.yiran.tetrajs.compat.tetrawear.TetraWearEnergyContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import se.mickelus.tetrawear.systems.energy.PlayerEnergy;
import se.mickelus.tetrawear.systems.energy.effects.EnergyMiningEffect;

@Mixin(value = EnergyMiningEffect.class, remap = false)
public class EnergyMiningEffectMixin {
    @WrapOperation(method = "onEnergyTick", at = @At(value = "INVOKE", target = "Lse/mickelus/tetrawear/systems/energy/PlayerEnergy;drain(D)V"))
    private static void tetrajs$drain(PlayerEnergy instance, double amount, Operation<Void> original) {
        TetraWearEnergyContext.run("mine", () -> original.call(instance, amount));
    }
}
