package net.yiran.tetrajs.requirements;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.yiran.tetrajs.client.ClientAdvancementHelper;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class AdvancementRequirement implements CraftingRequirement, CraftingEffectCondition {
    public ResourceLocation advancement;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] materials, Map<ToolAction, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        return hasAdvancement(player);
    }

    @Override
    public boolean test(CraftingContext cxt) {
        return hasAdvancement(cxt.player);
    }

    private boolean hasAdvancement(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            var holder = serverPlayer.getServer().getAdvancements().getAdvancement(advancement);
            return holder != null && serverPlayer.getAdvancements().getOrStartProgress(holder).isDone();
        }
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return ClientAdvancementHelper.hasAdvancement(advancement);
        }
        return false;
    }

    @Nullable
    @Override
    public List<Component> getDescription() {
        if (!advancement.getNamespace().equals("minecraft"))
            return List.of(Component.translatable("tetrajs.holo.advancement_requirement",
                    Component.translatable(advancement.getNamespace() + ".advancements." + advancement.getPath().replace("/", ".") + ".title")
            ));
        else
            return List.of(Component.translatable("tetrajs.holo.advancement_requirement",
                    Component.translatable("advancements." + advancement.getPath().replace("/", ".") + ".title")
            ));
    }

}
