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
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class TimeRequirement implements CraftingRequirement, CraftingEffectCondition {
    public String time;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] materials, Map<ToolAction, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        var timeOfDay = world.dayTime() % 24000;
        return switch (time) {
            case "day" -> isDaytime(timeOfDay);
            case "night" -> !isDaytime(timeOfDay);
            default -> false;
        };
    }

    @Override
    public boolean test(CraftingContext cxt) {
        Level level = cxt.world;
        if (level == null) return false;
        var timeOfDay = level.dayTime() % 24000;
        return switch (time) {
            case "day" -> isDaytime(timeOfDay);
            case "night" -> !isDaytime(timeOfDay);
            default -> false;
        };
    }

    public static boolean isDaytime(long timeOfDay) {
        return timeOfDay >= 23950 || timeOfDay < 12750;
    }

    @Nullable
    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("tetrajs.holo.time_requirement", Component.translatable("tetrajs.holo.time_requirement." + time)));
    }
}
