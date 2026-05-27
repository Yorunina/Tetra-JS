package net.yiran.tetrajs.core.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;
import net.yiran.tetrajs.client.ModuleExtendInfoButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.GuiSchematicDetail;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Map;

@Mixin(GuiSchematicDetail.class)
public class GuiSchematicDetailMixin extends GuiElement {
    @Unique
    private ModuleExtendInfoButton moduleExtendInfoButton;

    public GuiSchematicDetailMixin(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void GuiSchematicDetail(int x, int y, Runnable backListener, Runnable craftListener, CallbackInfo ci) {
        this.moduleExtendInfoButton = new ModuleExtendInfoButton(3, 3, 16, 16);
        this.addChild(this.moduleExtendInfoButton);
    }

    @Inject(method = "update", at = @At("TAIL"), remap = false)
    public void update(Level level, BlockPos pos, WorkbenchTile blockEntity, UpgradeSchematic schematic, ItemStack itemStack, String slot, ItemStack[] materials, Map<ToolAction, Integer> availableTools, Player player, CallbackInfo ci) {
        this.moduleExtendInfoButton.update(level, pos, blockEntity, itemStack, slot, schematic, player);
    }
}
