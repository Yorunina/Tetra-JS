package net.yiran.tetrajs.core.mixins.tetrawear;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.compat.tetrawear.TetraWearEnergyContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import se.mickelus.tetrawear.systems.energy.effects.MissedSwingDrainEnergyPacket;

@Mixin(value = MissedSwingDrainEnergyPacket.class, remap = false)
public class MissedSwingDrainEnergyPacketMixin {
    @Shadow
    boolean onlyCooldown;

    @WrapMethod(method = "handle")
    private void tetrajs$handle(Player player, Operation<Void> original) {
        if (this.onlyCooldown) {
            original.call(player);
            return;
        }
        TetraWearEnergyContext.run("miss", () -> original.call(player));
    }
}
