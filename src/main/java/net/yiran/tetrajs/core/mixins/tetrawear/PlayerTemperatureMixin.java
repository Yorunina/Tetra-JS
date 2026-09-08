package net.yiran.tetrajs.core.mixins.tetrawear;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.compat.tetrawear.TemperaturePenaltyAccess;
import net.yiran.tetrajs.kubejs.events.TemperatureUpdateEventJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetrawear.systems.temperature.PlayerTemperature;
import se.mickelus.tetrawear.systems.temperature.TemperaturePenalty;
import se.mickelus.tetrawear.systems.temperature.TemperatureSystem;

@Mixin(value = PlayerTemperature.class, remap = false)
public class PlayerTemperatureMixin implements TemperaturePenaltyAccess {
    @Shadow
    private float current;
    @Shadow
    private float target;
    @Unique
    private TemperaturePenalty tetrajs$penaltyOverride;
    @Unique
    private boolean tetrajs$hasPenaltyOverride;

    @Inject(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lse/mickelus/tetrawear/systems/temperature/PlayerTemperature;generateHealth(Lnet/minecraft/world/entity/player/Player;)V")
    )
    private void tetrajs$onTick(Player player, CallbackInfo ci) {
        if (player.level().isClientSide()) {
            return;
        }
        BlockPos pos = player.blockPosition();
        int zone = TemperatureSystem.getTempZone(this.current);
        int heatResistance = TemperatureSystem.getHeatResistance(player);
        int coldResistance = TemperatureSystem.getColdResistance(player);
        TemperaturePenalty penalty = TemperaturePenalty.get(zone, heatResistance, coldResistance);
        TemperatureUpdateEventJS event = new TemperatureUpdateEventJS(
                player,
                this.current,
                this.target,
                player.level().getBiome(pos).value().getBaseTemperature() * 100.0F,
                getTimeMultiplier(player.level().getDayTime() % 24000L),
                zone,
                heatResistance,
                coldResistance,
                penalty == null ? null : penalty.name()
        );
        TetraJSEvents.TemperatureUpdate.post(event);
        this.target = event.getTarget();
        this.tetrajs$setPenaltyOverride(parsePenalty(event.getPenalty()), event.isPenaltyModified());
    }

    @Override
    public void tetrajs$setPenaltyOverride(TemperaturePenalty penalty, boolean present) {
        this.tetrajs$hasPenaltyOverride = present;
        this.tetrajs$penaltyOverride = present ? penalty : null;
    }

    @Override
    public boolean tetrajs$hasPenaltyOverride() {
        return this.tetrajs$hasPenaltyOverride;
    }

    @Override
    public TemperaturePenalty tetrajs$getPenaltyOverride() {
        return this.tetrajs$penaltyOverride;
    }

    @Unique
    private static float getTimeMultiplier(long time) {
        if (time < 6000L) {
            return 0.6F + (float) time / 6000.0F * 0.4F;
        }
        if (time > 22000L) {
            return 0.4F + (float) (time - 22000L) / 2000.0F * 0.2F;
        }
        if (time > 14000L) {
            return 1.0F - (float) (time - 14000L) / 8000.0F * 0.6F;
        }
        return 1.0F;
    }

    @Unique
    private static TemperaturePenalty parsePenalty(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return TemperaturePenalty.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
