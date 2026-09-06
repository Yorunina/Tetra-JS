package net.yiran.tetrajs.requirements;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MultiblockRequirement implements CraftingRequirement, CraftingEffectCondition {
    public Map<Block, Integer> blocks;
    public int x = 3;
    public int y = 2;
    public int z = 3;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] materials, Map<ToolAction, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        return test(world, pos);
    }

    @Override
    public boolean test(CraftingContext cxt) {
        Level level = cxt.world;
        if (level == null) return false;
        BlockPos pos = cxt.pos;
        if (pos == null) return false;
        return test(level, pos);
    }

    private boolean test(Level world, BlockPos pos) {
        AABB aabb = new AABB(pos).inflate(x, y, z);
        Map<Block, Integer> map = world.getBlockStatesIfLoaded(aabb).map(BlockBehaviour.BlockStateBase::getBlock).collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(i -> 1)));
        for (Map.Entry<Block, Integer> entry : blocks.entrySet()) {
            if (map.getOrDefault(entry.getKey(), 0) < entry.getValue()) return false;
        }
        return true;
    }

    @Nullable
    @Override
    public List<Component> getDescription() {
        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        int size = blocks.size();
        int index = 0;
        builder.add(Component.translatable("tetrajs.holo.multiblock_requirement", x * 2 + 1, y * 2 + 1, z * 2 + 1));
        for (Map.Entry<Block, Integer> entry : blocks.entrySet()) {
            index++;

            if (index == size) {
                builder.add(Component.literal(" §8└§r ").append(entry.getKey().getName()).append(" x" + entry.getValue()));
            } else {
                builder.add(Component.literal(" §8├§r ").append(entry.getKey().getName()).append(" x" + entry.getValue()));
            }
        }
        return builder.build();
    }
}
