package net.yiran.tetrajs.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class ModuleExtendInfoButton extends GuiElement {
    private List<Component> tooltip;
    public ModuleExtendInfoButton(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public List<Component> getTooltipLines() {
        return tooltip;
    }

    public void update(Level level, BlockPos pos, WorkbenchTile blockEntity, ItemStack itemStack, String slot, UpgradeSchematic schematic, Player playerEntity) {
        tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tetra.holo.craft.applicable_materials"));
        tooltip.add(Component.literal(""));
        Item item = itemStack.getItem();
        if (item instanceof ModularItem modularItem) {
            ItemModule module = modularItem.getModuleFromSlot(itemStack, slot);
            module.getAttributeModifiers(itemStack).forEach((attribute, modifier) -> tooltip.add(Component.translatable("tetra.module.extend_info.attribute_modifier", Component.translatable(attribute.getDescriptionId()), modifier.getAmount())));
        }
    }

}
