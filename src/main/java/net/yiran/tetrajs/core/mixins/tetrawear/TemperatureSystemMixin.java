package net.yiran.tetrajs.core.mixins.tetrawear;

import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.compat.tetrawear.TemperaturePenaltyAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetrawear.systems.temperature.IPlayerTemperature;
import se.mickelus.tetrawear.systems.temperature.TemperaturePenalty;
import se.mickelus.tetrawear.systems.temperature.TemperatureSystem;

@Mixin(value = TemperatureSystem.class, remap = false)
public class TemperatureSystemMixin {
    @Inject(
            method = "getTemperaturePenalty(Lse/mickelus/tetrawear/systems/temperature/IPlayerTemperature;Lnet/minecraft/world/entity/player/Player;)Lse/mickelus/tetrawear/systems/temperature/TemperaturePenalty;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tetrajs$getPenalty(IPlayerTemperature temperature, Player player, CallbackInfoReturnable<TemperaturePenalty> cir) {
        if (temperature instanceof TemperaturePenaltyAccess access && access.tetrajs$hasPenaltyOverride()) {
            cir.setReturnValue(access.tetrajs$getPenaltyOverride());
        }
    }
}
