package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ItemBrokenEventJS extends EventJS {
    private final LivingEntity entity;
    private final ItemStack beforeStack;
    private final ItemStack afterStack;
    private final int damage;
    private final InteractionHand hand;

    public ItemBrokenEventJS(LivingEntity entity, ItemStack beforeStack, ItemStack afterStack, int damage, InteractionHand hand) {
        this.entity = entity;
        this.beforeStack = beforeStack.copy();
        this.afterStack = afterStack.copy();
        this.damage = damage;
        this.hand = hand;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ItemStack getBeforeStack() {
        return beforeStack;
    }

    public ItemStack getAfterStack() {
        return afterStack;
    }

    public ItemStack getStack() {
        return beforeStack;
    }

    public int getDamage() {
        return damage;
    }

    public InteractionHand getHand() {
        return hand;
    }
}
