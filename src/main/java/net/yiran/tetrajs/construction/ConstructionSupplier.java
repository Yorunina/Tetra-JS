package net.yiran.tetrajs.construction;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class ConstructionSupplier {
    private static final int SHULKER_SLOTS = 27;

    private final Player player;
    private final ConstructionOptions options;
    private final Map<BlockItem, Integer> counts = new LinkedHashMap<>();
    private final List<BlockItem> pool = new ArrayList<>();

    public ConstructionSupplier(Player player, ConstructionOptions options) {
        this.player = player;
        this.options = options;
    }

    public void gather(BlockItem target) {
        counts.clear();
        pool.clear();
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() instanceof BlockItem offhandBlock) {
            add(offhandBlock);
            return;
        }
        if (target == null || target == Items.AIR) {
            return;
        }
        add(target);
        if (options.getMatch() != ConstructionOptions.Match.EXACT) {
            for (Item item : SimilarBlocks.matchingItems(target)) {
                if (item instanceof BlockItem blockItem) {
                    add(blockItem);
                }
            }
        }
    }

    public int available() {
        if (player.getAbilities().instabuild) {
            return Integer.MAX_VALUE;
        }
        int total = 0;
        for (int count : counts.values()) {
            total += count;
        }
        return total;
    }

    public BlockItem takePreview(Predicate<BlockItem> canPlace) {
        if (player.getAbilities().instabuild) {
            for (BlockItem item : pool) {
                if (canPlace.test(item)) {
                    return item;
                }
            }
            return null;
        }
        for (int i = 0; i < pool.size(); i++) {
            BlockItem item = pool.get(i);
            int count = counts.getOrDefault(item, 0);
            if (count <= 0 || !canPlace.test(item)) {
                continue;
            }
            counts.put(item, count - 1);
            if (count - 1 <= 0) {
                pool.remove(i);
            }
            return item;
        }
        return null;
    }

    public boolean consume(Item item) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        int remaining = takeFromInv(item, false, false, 1);
        remaining = takeFromInv(item, false, true, remaining);
        remaining = takeFromInv(item, true, true, remaining);
        remaining = takeFromInv(item, true, false, remaining);
        return remaining == 0;
    }

    public static int count(Player player, Item item) {
        if (player.getAbilities().instabuild) {
            return Integer.MAX_VALUE;
        }
        int total = 0;
        for (ItemStack stack : fullInventory(player)) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(item)) {
                total += stack.getCount();
            } else {
                total += countInContainer(stack, item);
            }
        }
        return total;
    }

    private void add(BlockItem item) {
        int count = count(player, item);
        if (count > 0 && !counts.containsKey(item)) {
            counts.put(item, count);
            pool.add(item);
        }
    }

    private int takeFromInv(Item item, boolean hotbar, boolean container, int remaining) {
        List<ItemStack> stacks = hotbar ? hotbarWithOffhand(player) : mainInventory(player);
        for (ItemStack stack : stacks) {
            if (remaining == 0) {
                break;
            }
            if (container) {
                remaining = useInContainer(stack, item, remaining);
            } else if (stack.is(item)) {
                stack.shrink(1);
                remaining = 0;
                player.getInventory().setChanged();
            }
        }
        return remaining;
    }

    private static int countInContainer(ItemStack container, Item item) {
        if (isShulker(container)) {
            int total = 0;
            for (ItemStack stack : readShulker(container)) {
                if (stack.is(item)) {
                    total += stack.getCount();
                }
            }
            return total;
        }
        Optional<IItemHandler> handler = container.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handler.isEmpty()) {
            return 0;
        }
        int total = 0;
        IItemHandler inventory = handler.get();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int useInContainer(ItemStack container, Item item, int remaining) {
        if (isShulker(container)) {
            NonNullList<ItemStack> items = readShulker(container);
            boolean changed = false;
            for (ItemStack stack : items) {
                if (stack.is(item)) {
                    int take = Math.min(remaining, stack.getCount());
                    stack.shrink(take);
                    remaining -= take;
                    changed = true;
                    if (remaining == 0) {
                        break;
                    }
                }
            }
            if (changed) {
                writeShulker(container, items);
            }
            return remaining;
        }
        Optional<IItemHandler> handler = container.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handler.isEmpty()) {
            return remaining;
        }
        IItemHandler inventory = handler.get();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.is(item)) {
                continue;
            }
            ItemStack extracted = inventory.extractItem(slot, remaining, false);
            remaining -= extracted.getCount();
            if (remaining <= 0) {
                break;
            }
        }
        return remaining;
    }

    private static boolean isShulker(ItemStack stack) {
        return stack.getCount() == 1 && Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock;
    }

    private static NonNullList<ItemStack> readShulker(ItemStack stack) {
        NonNullList<ItemStack> items = NonNullList.withSize(SHULKER_SLOTS, ItemStack.EMPTY);
        CompoundTag root = stack.getTag();
        if (root != null && root.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            CompoundTag entityTag = root.getCompound("BlockEntityTag");
            if (entityTag.contains("Items", Tag.TAG_LIST)) {
                ContainerHelper.loadAllItems(entityTag, items);
            }
        }
        return items;
    }

    private static void writeShulker(ItemStack stack, NonNullList<ItemStack> items) {
        CompoundTag root = stack.getOrCreateTag();
        if (!root.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            root.put("BlockEntityTag", new CompoundTag());
        }
        ContainerHelper.saveAllItems(root.getCompound("BlockEntityTag"), items);
    }

    private static List<ItemStack> fullInventory(Player player) {
        ArrayList<ItemStack> stacks = new ArrayList<>(player.getInventory().offhand);
        stacks.addAll(player.getInventory().items);
        return stacks;
    }

    private static List<ItemStack> hotbarWithOffhand(Player player) {
        ArrayList<ItemStack> stacks = new ArrayList<>(player.getInventory().items.subList(0, 9));
        stacks.addAll(player.getInventory().offhand);
        return stacks;
    }

    private static List<ItemStack> mainInventory(Player player) {
        return player.getInventory().items.subList(9, player.getInventory().items.size());
    }
}
