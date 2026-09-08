package net.yiran.tetrajs.core.mixins.tetrawear;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.compat.tetrawear.TetraWearDodgeContext;
import net.yiran.tetrajs.compat.tetrawear.TetraWearEnergyContext;
import net.yiran.tetrajs.kubejs.events.DodgeEventJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetrawear.ConfigHandler;
import se.mickelus.tetrawear.abilities.dodge.Dodge;
import se.mickelus.tetrawear.abilities.dodge.DodgeAttributes;
import se.mickelus.tetrawear.abilities.dodge.DodgeDirection;

@Mixin(value = Dodge.class, remap = false)
public class DodgeMixin {
    @WrapMethod(method = "dodgePlayer")
    private static void tetrajs$dodgePlayer(Player player, DodgeDirection direction, Operation<Void> original) {
        boolean enabled = Dodge.isEnabled(player);
        boolean energyEnough = Dodge.checkEnergy(player);
        if (!enabled || !energyEnough) {
            original.call(player, direction);
            return;
        }
        if (player.level().isClientSide()) {
            original.call(player, direction);
            return;
        }
        double strength = player.getAttributeValue(DodgeAttributes.dodgeStrength.get())
                * ConfigHandler.server.dodgeHorizontalStrength.get();
        double energyCost = player.getAttributeValue(DodgeAttributes.dodgeEnergyCost.get())
                * ConfigHandler.server.dodgeEnergyCost.get();
        double saturationCost = ConfigHandler.server.dodgeSaturationCost.get();
        DodgeEventJS event = new DodgeEventJS(player, direction.name(), strength, energyCost, saturationCost);
        if (TetraJSEvents.Dodge.post(event).interruptFalse()) {
            return;
        }
        TetraWearDodgeContext.set(event.getStrength(), event.getEnergyCost(), event.getSaturationCost());
        try {
            TetraWearEnergyContext.run("dodge", () -> original.call(player, direction));
        } finally {
            TetraWearDodgeContext.clear();
        }
    }

    @Inject(method = "getDodgeStrength", at = @At("RETURN"), cancellable = true)
    private static void tetrajs$overrideStrength(Player entity, CallbackInfoReturnable<Double> cir) {
        Double strength = TetraWearDodgeContext.getStrength();
        if (strength != null) {
            cir.setReturnValue(strength);
        }
    }

    @Inject(method = "getEnergyCost", at = @At("RETURN"), cancellable = true)
    private static void tetrajs$overrideEnergyCost(Player entity, CallbackInfoReturnable<Double> cir) {
        Double energyCost = TetraWearDodgeContext.getEnergyCost();
        if (energyCost != null) {
            cir.setReturnValue(energyCost);
        }
    }

    @Inject(method = "drainEnergy", at = @At("HEAD"), cancellable = true)
    private static void tetrajs$skipZeroEnergy(Player entity, CallbackInfo ci) {
        Double energyCost = TetraWearDodgeContext.getEnergyCost();
        if (energyCost != null && energyCost <= 0.0) {
            ci.cancel();
        }
    }

    @ModifyArg(
            method = "dodgePlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V", remap = true),
            index = 0
    )
    private static float tetrajs$overrideSaturation(float original) {
        Double saturationCost = TetraWearDodgeContext.getSaturationCost();
        return saturationCost != null ? saturationCost.floatValue() : original;
    }
}
