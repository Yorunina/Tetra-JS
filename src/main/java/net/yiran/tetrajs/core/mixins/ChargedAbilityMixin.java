package net.yiran.tetrajs.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.yiran.tetrajs.abilities.ChargedAbilityRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

@Mixin(value = ItemModularHandheld.class, remap = false)
public class ChargedAbilityMixin {
    @Inject(method = "getChargeableAbility", at = @At("HEAD"), cancellable = true)
    private void tetrajs$selectCustomAbility(ItemStack stack, CallbackInfoReturnable<ChargedAbilityEffect> cir) {
        ChargedAbilityEffect ability = ChargedAbilityRegistry.findAvailable((ItemModularHandheld) (Object) this, stack);
        if (ability != null) {
            cir.setReturnValue(ability);
        }
    }

    @ModifyReturnValue(method = "m_8105_", at = @At("RETURN"))
    private int tetrajs$customUseDuration(int original, ItemStack stack) {
        if (original > 0) {
            return original;
        }
        return ChargedAbilityRegistry.hasAvailable((ItemModularHandheld) (Object) this, stack) ? 72000 : original;
    }

    @Inject(method = "handleChargedAbility", at = @At("HEAD"), cancellable = true)
    private static void tetrajs$performCustomAbility(Player player, InteractionHand hand, LivingEntity target,
                                                      BlockPos block, Vec3 hit, int chargedTicks, CallbackInfo ci) {
        if (ChargedAbilityRegistry.perform(player, hand, target, block, hit, chargedTicks)) {
            ci.cancel();
        }
    }
}
