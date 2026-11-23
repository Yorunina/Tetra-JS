package net.yiran.tetrajs.requirements;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.ForgeRegistries;
import net.yiran.tetrajs.TetraJS;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class EnchantmentRequirement implements CraftingRequirement, CraftingEffectCondition {
    public ResourceLocation enchantment;
    public int level = 1;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] materials, Map<ToolAction, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        return hasEnchantment(upgradedStack);
    }

    @Override
    public boolean test(CraftingContext cxt) {
        return hasEnchantment(cxt.targetStack);
    }

    @Nullable
    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("tetrajs.holo.enchantment_requirement", enchantment, level));
    }

    private boolean hasEnchantment(ItemStack stack) {
        Enchantment resolved = resolveEnchantment();
        return resolved != null && stack.getEnchantmentLevel(resolved) >= level;
    }

    @Nullable
    private Enchantment resolveEnchantment() {
        if (enchantment == null) {
            TetraJS.LOGGER.warn("Invalid enchantment ID in tetrajs:enchantment requirement: <null>");
            return null;
        }
        Enchantment resolved = ForgeRegistries.ENCHANTMENTS.getValue(enchantment);
        if (resolved == null) {
            TetraJS.LOGGER.warn("Invalid enchantment ID in tetrajs:enchantment requirement: {}", enchantment);
        }
        return resolved;
    }
}