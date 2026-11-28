package net.yiran.tetrajs.core.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.yiran.tetrajs.kubejs.events.ItemBrokenEventJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import org.spongepowered.asm.mixin.Mixin;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackDamageMixin {
    @WrapMethod(method = "hurtAndBreak")
    private void tetrajs$itemBroken(int amount, LivingEntity entity, Consumer<LivingEntity> onBroken,
                                    Operation<Void> original) {
        ItemStack stack = (ItemStack) (Object) this;
        if (entity == null || entity.level().isClientSide() || amount <= 0
                || !(stack.getItem() instanceof IModularItem modularItem)) {
            original.call(amount, entity, onBroken);
            return;
        }

        boolean wasBroken = modularItem.isBroken(stack);
        ItemStack beforeStack = stack.copy();
        InteractionHand hand = findHand(entity, stack);

        original.call(amount, entity, onBroken);

        if (wasBroken) {
            return;
        }

        if (stack.isEmpty() || modularItem.isBroken(stack)) {
            TetraJSEvents.ItemBroken.post(new ItemBrokenEventJS(entity, beforeStack, stack, amount, hand));
        }
    }

    private static InteractionHand findHand(LivingEntity entity, ItemStack stack) {
        if (entity.getMainHandItem() == stack) {
            return InteractionHand.MAIN_HAND;
        }
        if (entity.getOffhandItem() == stack) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
