package net.yiran.tetrajs.requirements;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AllImprovementRequirement implements CraftingRequirement, CraftingEffectCondition {
    public String improvement;
    public int level;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] materials, Map<ToolAction, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        ModularItem modularItem = (ModularItem) upgradedStack.getItem();
        return Arrays.stream(modularItem.getImprovements(upgradedStack))
                .filter(pImprovement -> pImprovement.key.equals(improvement))
                .anyMatch(pImprovement -> level <= 0 || pImprovement.getLevel() == level);
    }

    @Override
    public boolean test(CraftingContext ctx) {
        ModularItem modularItem = (ModularItem) ctx.targetStack.getItem();
        return Arrays.stream(modularItem.getImprovements(ctx.targetStack))
                .filter(pImprovement -> pImprovement.key.equals(improvement))
                .anyMatch(pImprovement -> level <= 0 || pImprovement.getLevel() == level);
    }

    @Nullable
    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("tetrajs.holo.all_improvement_requirement", improvement, level));
    }
}
