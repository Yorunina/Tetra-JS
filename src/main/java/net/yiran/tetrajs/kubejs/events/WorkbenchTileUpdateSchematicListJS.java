package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.player.Player;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkbenchTileUpdateSchematicListJS extends EventJS {
    public Player player;
    public WorkbenchTile workbenchTile;
    public String selectedSlot;
    public List<UpgradeSchematic> schematicList;

    public WorkbenchTileUpdateSchematicListJS(Player player, WorkbenchTile workbenchTile, String selectedSlot, UpgradeSchematic[] schematicList) {
        this.player = player;
        this.workbenchTile = workbenchTile;
        this.selectedSlot = selectedSlot;
        this.schematicList = new ArrayList<>(Arrays.asList(schematicList));
    }

    public Player getPlayer() {
        return player;
    }

    public WorkbenchTile getWorkbenchTile() {
        return workbenchTile;
    }

    public String getSelectedSlot() {
        return selectedSlot;
    }

    public List<UpgradeSchematic> getSchematicList() {
        return schematicList;
    }
}
