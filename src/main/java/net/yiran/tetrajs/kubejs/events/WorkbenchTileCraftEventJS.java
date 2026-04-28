package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

public class WorkbenchTileCraftEventJS extends EventJS {
    public ItemStack targetStack;
    public ItemStack upgradedStack;
    public Player player;
    public Level level;
    public WorkbenchTile workbenchTile;
    public ItemStack[] materials;
    public ItemStack[] materialsAltered;
    public UpgradeSchematic currentSchematic;
    public String currentSlot;
    private boolean itemModified = false;

    public WorkbenchTileCraftEventJS(
            ItemStack targetStack,
            ItemStack upgradedStack,
            Player player,
            WorkbenchTile workbenchTile,
            ItemStack[] materials,
            ItemStack[] materialsAltered,
            UpgradeSchematic currentSchematic,
            String currentSlot
    ) {
        this.targetStack = targetStack;
        this.upgradedStack = upgradedStack;
        this.player = player;
        this.level = player.level();
        this.workbenchTile = workbenchTile;
        this.materials = materials;
        this.materialsAltered = materialsAltered;
        this.currentSchematic = currentSchematic;
        this.currentSlot = currentSlot;
    }

    public ItemStack getTargetStack() {
        return targetStack;
    }

    public ItemStack getUpgradedStack() {
        return upgradedStack;
    }

    public Player getPlayer() {
        return player;
    }

    public Level getLevel() {
        return level;
    }

    public WorkbenchTile getWorkbenchTile() {
        return workbenchTile;
    }

    public ItemStack[] getMaterials() {
        return materials;
    }

    public ItemStack[] getMaterialsAltered() {
        return materialsAltered;
    }

    public UpgradeSchematic getCurrentSchematic() {
        return currentSchematic;
    }

    public String getCurrentSlot() {
        return currentSlot;
    }

    public void setUpgradedStack(ItemStack upgradedStack) {
        this.upgradedStack = upgradedStack;
        this.itemModified = true;
    }
    public boolean isItemModified() {
        return this.itemModified;
    }
}
