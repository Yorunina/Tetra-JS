package net.yiran.tetrajs.crafteffect;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.craftingeffect.outcome.CraftingEffectOutcome;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.HashMap;
import java.util.Map;

public class CustomCraftingEffectOutcome implements CraftingEffectOutcome {
    public static CustomFunction DEFAULT = context -> {
    };
    public static Map<String, CustomFunction> Functions = new HashMap<>();
    public String key;

    public static void register(String key, CustomFunction cxt) {
        Functions.put(key, cxt);
    }

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources, ItemStack[] postMaterials, float v) {
        Functions.getOrDefault(key, DEFAULT).test(new Context(unlockedEffects, upgradedStack, slot, isReplacing, player, preMaterials, tools, world, schematic, pos, blockState, consumeResources, postMaterials));
        return true;
    }

    public interface CustomFunction {
        void test(Context context);
    }

    public record Context(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing,
                          Player player, ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world,
                          UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources,
                          ItemStack[] postMaterials) {
    }
}
