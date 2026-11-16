package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ItemBrokenEventJS extends EventJS {
    public final ItemStack beforeStack;
    public final ItemStack afterStack;
    public final LivingEntity usingEntity;
    public final int amount;

    public ItemBrokenEventJS(ItemStack beforeStack, ItemStack afterStack, LivingEntity usingEntity, int amount) {
        this.beforeStack = beforeStack;
        this.afterStack = afterStack;
        this.usingEntity = usingEntity;
        this.amount = amount;
    }

    public ItemStack getBeforeStack() {
        return beforeStack;
    }

    public ItemStack getAfterStack() {
        return afterStack;
    }

    public LivingEntity getUsingEntity() {
        return usingEntity;
    }

    public int getAmount() {
        return amount;
    }

}
