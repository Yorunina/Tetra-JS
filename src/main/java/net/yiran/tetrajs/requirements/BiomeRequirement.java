package net.yiran.tetrajs.requirements;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BiomeRequirement implements CraftingRequirement, CraftingEffectCondition {
    public ResourceLocation biome;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] materials, Map<ToolAction, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        Biome biome1 = world.getBiome(pos).get();
        return Objects.equals(world.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome1), biome);
    }

    @Override
    public boolean test(CraftingContext cxt) {
        Level level = cxt.world;
        if (level == null) return false;
        BlockPos pos = cxt.pos;
        if (pos == null) return false;
        Biome biome1 = level.getBiome(pos).get();
        return Objects.equals(level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome1), biome);
    }

    @Nullable
    @Override
    public List<Component> getDescription() {
        String key = "biome." + biome.getNamespace() + "." + biome.getPath();
        return List.of(Component.translatable("tetrajs.holo.biome_requirement", Component.translatable(key)));
    }
}
