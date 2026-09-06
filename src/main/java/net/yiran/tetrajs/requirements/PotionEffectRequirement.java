package net.yiran.tetrajs.requirements;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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

public class PotionEffectRequirement implements CraftingRequirement, CraftingEffectCondition {
    public MobEffect effect;
    public int duration = 0;
    public int amplifier = 0;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] materials, Map<ToolAction, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        MobEffectInstance mobEffectInstance = player.getEffect(effect);
        return mobEffectInstance != null && mobEffectInstance.getAmplifier() >= amplifier && mobEffectInstance.getDuration() >= duration;
    }

    @Override
    public boolean test(CraftingContext cxt) {
        Player player = cxt.player;
        if (player == null) return false;
        MobEffectInstance mobEffectInstance = player.getEffect(effect);
        return mobEffectInstance != null && mobEffectInstance.getAmplifier() >= amplifier && mobEffectInstance.getDuration() >= duration;
    }

    @Nullable
    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("tetrajs.holo.potion_effect_requirement", effect.getDisplayName(), amplifier + 1, duration / 20));
    }
}
