package net.yiran.tetrajs.core.mixins;

import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import net.yiran.tetrajs.kubejs.events.WorkbenchTileCraftEventJS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Map;
import java.util.Objects;

@Mixin(WorkbenchTile.class)
public class WorkbenchTileMixin {
    @Shadow
    private String currentSlot;

    @Shadow
    private UpgradeSchematic currentSchematic;

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/util/LazyOptional;ifPresent(Lnet/minecraftforge/common/util/NonNullConsumer;)V"),
            method = "craft",
            remap = false,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void craft(Player player, CallbackInfo ci, ItemStack targetStack, ItemStack upgradedStack, IModularItem item, BlockState blockState, Map availableTools, ItemStack[] materials, ItemStack[] materialsAltered, ItemStack tempStack) {
        if (Objects.isNull(player)) return;
        TetraJSEvents.WorkbenchTileCraft.post(player.level().isClientSide() ? ScriptType.CLIENT : ScriptType.SERVER, new WorkbenchTileCraftEventJS(targetStack, upgradedStack, player, (WorkbenchTile) (Object) this, materials, materialsAltered, currentSchematic, currentSlot)).arch().isFalse();
    }


}