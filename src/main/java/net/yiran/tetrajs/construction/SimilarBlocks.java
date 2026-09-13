package net.yiran.tetrajs.construction;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SimilarBlocks {
    private static final List<Set<Item>> GROUPS = List.of(Set.of(
            Items.DIRT,
            Items.GRASS_BLOCK,
            Items.COARSE_DIRT,
            Items.PODZOL,
            Items.MYCELIUM,
            Items.FARMLAND,
            Items.DIRT_PATH,
            Items.ROOTED_DIRT
    ));

    private SimilarBlocks() {
    }

    public static boolean match(Block first, Block second) {
        if (first == second) {
            return true;
        }
        if (first == Blocks.AIR || second == Blocks.AIR) {
            return false;
        }
        Item firstItem = first.asItem();
        Item secondItem = second.asItem();
        for (Set<Item> group : GROUPS) {
            if (group.contains(firstItem) && group.contains(secondItem)) {
                return true;
            }
        }
        return false;
    }

    public static Set<Item> matchingItems(Item item) {
        HashSet<Item> result = new HashSet<>();
        for (Set<Item> group : GROUPS) {
            if (group.contains(item)) {
                result.addAll(group);
            }
        }
        result.remove(item);
        return result;
    }
}
