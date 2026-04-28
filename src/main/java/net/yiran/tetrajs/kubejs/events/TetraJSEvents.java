package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;

public interface TetraJSEvents {
    EventGroup GROUP = EventGroup.of("TetraJSEvents");
    EventHandler StatBarRegister = GROUP.client("registerStatBar",()-> StatBarRegisterEventJS.class);
    EventHandler EnchantAspectRegister = GROUP.startup("registerEnchantAspect",()-> EnchantAspectRegisterEventJS.class);
    EventHandler StatSorterRegister = GROUP.client("registerStatSorter",()-> StatSorterRegisterEventJS.class);
    EventHandler WorkbenchTileCraft = GROUP.common("workbenchTileCraft", () -> WorkbenchTileCraftEventJS.class);
    EventHandler WorkbenchTileUpdateSchematicList = GROUP.client("workbenchTileUpdateSchematicList", () -> WorkbenchTileUpdateSchematicListJS.class);
    EventHandler HammerBlockCraftConsumeTool = GROUP.server("hammerBlockCraftConsumeTool", () -> HammerBlockCraftConsumeToolJS.class).hasResult();
    EventHandler CreateArrow = GROUP.server("createArrow", () -> CreateArrowEventJS.class).extra(Extra.STRING);
}
