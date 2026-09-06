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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRequirement implements CraftingRequirement, CraftingEffectCondition {
    public static CustomFunction DEFAULT = cxt -> true;
    public static CustomCEFunction CE_DEFAULT = cxt -> true;
    public static Map<String, CustomFunction> Functions = new HashMap<>();
    public static Map<String, CustomCEFunction> CEFunctions = new HashMap<>();
    public String key;

    public static void register(String key, CustomFunction cxt) {
        Functions.put(key, cxt);
    }

    public static class Condition {
        public static void register(String key, CustomCEFunction cxt) {
            CEFunctions.put(key, cxt);
        }
    }

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] materials, Map<ToolAction, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        return CEFunctions.getOrDefault(key, CE_DEFAULT).test(new CEContext(unlocks, upgradedStack, slot, isReplacing, player, materials, tools, schematic, world, pos, blockState));
    }

    @Override
    public boolean test(CraftingContext craftingContext) {
        return Functions.getOrDefault(key, DEFAULT).test(craftingContext);
    }

    @Nullable
    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("tetrajs.holo.custom_requirement." + key));
    }

    @FunctionalInterface
    public interface CustomFunction {
        boolean test(CraftingContext craftingContext);
    }

    @FunctionalInterface
    public interface CustomCEFunction {
        boolean test(CEContext CEContext);
    }

    public record CEContext(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing,
                            Player player, ItemStack[] materials, Map<ToolAction, Integer> tools,
                            UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
    }
}
