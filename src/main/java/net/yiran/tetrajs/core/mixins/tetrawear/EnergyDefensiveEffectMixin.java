package net.yiran.tetrajs.core.mixins.tetrawear;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.compat.tetrawear.TetraWearEnergyContext;
import org.spongepowered.asm.mixin.Mixin;
import se.mickelus.tetrawear.systems.energy.effects.EnergyDefensiveEffect;

@Mixin(value = EnergyDefensiveEffect.class, remap = false)
public class EnergyDefensiveEffectMixin {
    @WrapMethod(method = "drainEnergy")
    private static void tetrajs$drainEnergy(Player player, double damage, Operation<Void> original) {
        TetraWearEnergyContext.run("defend", () -> original.call(player, damage));
    }
}
