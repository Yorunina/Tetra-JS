package net.yiran.tetrajs.core.mixins.tetrawear;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.compat.tetrawear.TetraWearEnergyContext;
import net.yiran.tetrajs.kubejs.events.EnergyDrainEventJS;
import net.yiran.tetrajs.kubejs.events.EnergyExhaustEventJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import se.mickelus.tetrawear.systems.energy.PlayerEnergy;

@Mixin(value = PlayerEnergy.class, remap = false)
public class PlayerEnergyMixin {
    @Shadow
    public double max;
    @Shadow
    public double current;
    @Shadow
    public boolean exhausted;
    @Shadow
    Player player;

    @WrapMethod(method = "drain")
    private void tetrajs$drain(double amount, Operation<Void> original) {
        if (this.player == null || this.player.level().isClientSide()) {
            original.call(amount);
            return;
        }
        String reason = TetraWearEnergyContext.getReason();
        EnergyDrainEventJS event = new EnergyDrainEventJS(this.player, amount, reason, this.current, this.max, this.exhausted);
        if (TetraJSEvents.EnergyDrain.post(event).interruptFalse()) {
            return;
        }
        boolean wasExhausted = this.exhausted;
        original.call(event.getAmount());
        if (!wasExhausted && this.exhausted) {
            TetraJSEvents.EnergyExhaust.post(new EnergyExhaustEventJS(this.player, this.current, this.max, reason, event.getAmount()));
        }
    }
}
