package net.yiran.tetrajs.sorter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;

import java.util.Arrays;

public class StatGetterItem implements IStatGetter {
    IStatGetter stat;
    Item[] items;
    TagKey<Item> tag;

    public StatGetterItem(IStatGetter stat, String[] items, String tag) {
        this.stat = stat;
        if (items != null) {
            this.items = Arrays.stream(items)
                    .map(ResourceLocation::new)
                    .map(ForgeRegistries.ITEMS::getValue)
                    .toArray(Item[]::new);
        }
        if (tag != null) {
            this.tag = ItemTags.create(new ResourceLocation(tag));
        }
    }

    private boolean check(ItemStack stack) {
        if (items != null) {
            for (Item item : items) {
                if (stack.is(item)) {
                    return true;
                }
            }
        }
        if (tag != null) {
            return stack.is(tag);
        }
        return false;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return check(itemStack) ? stat.getValue(player, itemStack) : 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String s) {
        return check(itemStack) ? stat.getValue(player, itemStack, s) : 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String s, String s1) {
        return check(itemStack) ? stat.getValue(player, itemStack, s, s1) : 0;
    }
}
