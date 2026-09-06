package net.yiran.tetrajs.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import net.yiran.tetrajs.kubejs.events.WorkbenchTileUpdateSchematicListJS;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.GuiSchematicList;
import se.mickelus.tetra.blocks.workbench.gui.GuiSlotDetail;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.SchematicRarity;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;

@Mixin(value = GuiSlotDetail.class, remap = false)
public class GuiSlotDetailMixin {
    @Shadow
    @Final
    private GuiSchematicList schematicList;

    /**
     * Tetra sorts the result of SchematicRegistry#getSchematics by getRarity(), getType(), and
     * getKey(). Filter malformed entries before that sort, while leaving Tetra's original
     * ordering and the existing KubeJS event injection unchanged.
     */
    @WrapOperation(
            method = "updateSchematicList",
            at = @At(
                    value = "INVOKE",
                    target = "Lse/mickelus/tetra/module/SchematicRegistry;getSchematics(Lse/mickelus/tetra/module/schematic/CraftingContext;)[Lse/mickelus/tetra/module/schematic/UpgradeSchematic;"
            ),
            remap = false
    )
    private UpgradeSchematic[] tetrajs$filterInvalidSchematics(
            CraftingContext context,
            Operation<UpgradeSchematic[]> original
    ) {
        UpgradeSchematic[] schematics = original.call(context);
        if (schematics == null || schematics.length == 0) {
            return schematics;
        }
        return Arrays.stream(schematics)
                .filter(GuiSlotDetailMixin::tetrajs$hasValidSortKeys)
                .toArray(UpgradeSchematic[]::new);
    }

    @Unique
    private static boolean tetrajs$hasValidSortKeys(UpgradeSchematic schematic) {
        if (schematic == null) {
            return false;
        }

        try {
            SchematicRarity rarity = schematic.getRarity();
            SchematicType type = schematic.getType();
            String key = schematic.getKey();
            if (rarity == null || type == null || key == null) {
                return false;
            }
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }


    @Inject(method = "updateSchematicList",
            at = @At(value = "INVOKE",
                    target = "Lse/mickelus/tetra/blocks/workbench/gui/GuiSchematicList;setSchematics([Lse/mickelus/tetra/module/schematic/UpgradeSchematic;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void updateSchematicList(Player player, WorkbenchTile tileEntity, String selectedSlot, CallbackInfo ci, CraftingContext context, UpgradeSchematic[] schematics) {
        WorkbenchTileUpdateSchematicListJS event = new WorkbenchTileUpdateSchematicListJS(player, tileEntity, selectedSlot, schematics);
        TetraJSEvents.WorkbenchTileUpdateSchematicList.post(event);
        UpgradeSchematic[] eventSchematics = event.schematicList == null
                ? new UpgradeSchematic[0]
                : event.schematicList.stream()
                .filter(GuiSlotDetailMixin::tetrajs$hasValidSortKeys)
                .toArray(UpgradeSchematic[]::new);
        this.schematicList.setSchematics(eventSchematics);
        ci.cancel();
    }
}
