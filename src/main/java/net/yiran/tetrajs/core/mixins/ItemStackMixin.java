package net.yiran.tetrajs.core.mixins;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.yiran.tetrajs.kubejs.events.ItemBrokenEventJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hurtAndBreak", at = @At("HEAD"))
    private void tetrajs$captureDamageState(int amount, LivingEntity usingEntity, Consumer<LivingEntity> onBreak, CallbackInfo ci,
                                            @Share("beforeStack") LocalRef<ItemStack> beforeStack,
                                            @Share("wasBroken") LocalBooleanRef wasBroken,
                                            @Share("wasEmpty") LocalBooleanRef wasEmpty) {
        ItemStack stack = (ItemStack) (Object) this;
        beforeStack.set(stack.copy());
        wasBroken.set(stack.getItem() instanceof IModularItem modularItem && modularItem.isBroken(stack));
        wasEmpty.set(stack.isEmpty());
    }

    @Inject(method = "hurtAndBreak", at = @At("RETURN"))
    private void tetrajs$publishBroken(int amount, LivingEntity usingEntity, Consumer<LivingEntity> onBreak, CallbackInfo ci,
                                       @Share("beforeStack") LocalRef<ItemStack> beforeStack,
                                       @Share("wasBroken") LocalBooleanRef wasBroken,
                                       @Share("wasEmpty") LocalBooleanRef wasEmpty) {
        if (amount <= 0 || wasBroken.get() || wasEmpty.get() || usingEntity.level().isClientSide) {
            return;
        }
        ItemStack stack = (ItemStack) (Object) this;
        ItemStack originalStack = beforeStack.get();
        if (!(originalStack.getItem() instanceof IModularItem modularItem)) {
            return;
        }
        if (!stack.isEmpty() && !modularItem.isBroken(stack)) {
            return;
        }
        TetraJSEvents.ItemBroken.post(new ItemBrokenEventJS(
                originalStack,
                stack.copy(),
                usingEntity,
                amount
        ));
    }
}
