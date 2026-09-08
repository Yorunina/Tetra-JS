package net.yiran.tetrajs.core.mixins.tetrawear;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.compat.tetrawear.TetraWearEnergyContext;
import org.spongepowered.asm.mixin.Mixin;
import se.mickelus.tetrawear.systems.energy.EnergyHelper;

@Mixin(value = EnergyHelper.class, remap = false)
public class EnergyHelperMixin {
    @WrapMethod(method = "drain")
    private static void tetrajs$drain(Player player, double cost, Operation<Void> original) {
        TetraWearEnergyContext.run("use", () -> original.call(player, cost));
    }
}
