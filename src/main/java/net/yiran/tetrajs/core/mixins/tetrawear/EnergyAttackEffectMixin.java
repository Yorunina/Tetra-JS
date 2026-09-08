package net.yiran.tetrajs.core.mixins.tetrawear;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.compat.tetrawear.TetraWearEnergyContext;
import org.spongepowered.asm.mixin.Mixin;
import se.mickelus.tetrawear.systems.energy.effects.EnergyAttackEffect;

@Mixin(value = EnergyAttackEffect.class, remap = false)
public class EnergyAttackEffectMixin {
    @WrapMethod(method = "drainEnergy")
    private static void tetrajs$drainEnergy(Player player, Operation<Void> original) {
        TetraWearEnergyContext.run("attack", () -> original.call(player));
    }
}
