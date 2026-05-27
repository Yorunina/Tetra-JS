package net.yiran.tetrajs.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(WorkbenchScreen.class)
public class WorkbenchScreenMixin {
    @WrapOperation(method = "buildPreviewStack", at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/aspect/TetraEnchantmentHelper;removeEnchantments(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)V"), remap = false)
    private void removeEnchantments(ItemStack itemStack, String slot, Operation<Void> original) {}

    @Inject(method = "buildPreviewStack", at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/blocks/workbench/WorkbenchTile;applyCraftingBonusEffects(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;ZLnet/minecraft/world/entity/player/Player;[Lnet/minecraft/world/item/ItemStack;[Lnet/minecraft/world/item/ItemStack;Ljava/util/Map;Lse/mickelus/tetra/module/schematic/UpgradeSchematic;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZF)Lnet/minecraft/world/item/ItemStack;"), remap = false)
    private void doRemoveEnchantments(UpgradeSchematic schematic, ItemStack targetStack, String slot, ItemStack[] materials, CallbackInfoReturnable<ItemStack> cir, @Local(name = "willReplace") boolean willReplace, @Local(name = "result") ItemStack upgradedStack) {
        if (!willReplace) return;

        CompoundTag map = upgradedStack.getTagElement("EnchantmentMapping");
        if (map == null) return;

        ListTag enchantments = Optional.ofNullable(upgradedStack.getTag()).map((tag) -> tag.getList("Enchantments", 10)).orElse(null);
        ItemModule module = ((IModularItem)upgradedStack.getItem()).getModuleFromSlot(upgradedStack, slot);
        if (enchantments != null && module instanceof ItemModuleMajor majorModule) {
            Set<String> matchingEnchantments = map.getAllKeys().stream().filter((ench) -> slot.equals(map.getString(ench))).filter((ench) -> !majorModule.acceptsEnchantment(upgradedStack, ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.parse(ench)), false)).collect(Collectors.toSet());
            enchantments.removeIf((nbt) -> matchingEnchantments.contains(((CompoundTag)nbt).getString("id")));
            if (!matchingEnchantments.isEmpty()) {
                matchingEnchantments.forEach(map::remove);
            }
        }
    }
}
