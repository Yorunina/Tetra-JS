package net.yiran.tetrajs.core.mixins;

import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import net.yiran.tetrajs.kubejs.events.WorkbenchTileUpdateSchematicListJS;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.GuiSchematicList;
import se.mickelus.tetra.blocks.workbench.gui.GuiSlotDetail;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

@Mixin(value = GuiSlotDetail.class, remap = false)
public class GuiSlotDetailMixin {
    @Shadow
    @Final
    private GuiSchematicList schematicList;

    @Inject(method = "updateSchematicList",
            at = @At(value = "INVOKE",
                    target = "Lse/mickelus/tetra/blocks/workbench/gui/GuiSchematicList;setSchematics([Lse/mickelus/tetra/module/schematic/UpgradeSchematic;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void updateSchematicList(Player player, WorkbenchTile tileEntity, String selectedSlot, CallbackInfo ci, CraftingContext context, UpgradeSchematic[] schematics) {
        WorkbenchTileUpdateSchematicListJS event = new WorkbenchTileUpdateSchematicListJS(player, tileEntity, selectedSlot, schematics);
        TetraJSEvents.WorkbenchTileUpdateSchematicList.post(event);
        this.schematicList.setSchematics(event.schematicList.toArray(new UpgradeSchematic[0]));
        ci.cancel();
    }
}
