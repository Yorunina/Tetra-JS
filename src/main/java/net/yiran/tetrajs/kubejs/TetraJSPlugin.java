package net.yiran.tetrajs.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraftforge.common.ToolAction;
import net.yiran.tetrajs.api.*;
import net.yiran.tetrajs.compat.CompatManager;
import net.yiran.tetrajs.kubejs.builders.items.*;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import net.yiran.tetrajs.util.NbtSlotData;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.SchematicRegistry;

public class TetraJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        RegistryInfo.ITEM.addType("TetraJS:BaseModularItem", BaseModularItemBuilder.class, BaseModularItemBuilder::new);
        RegistryInfo.ITEM.addType("TetraJS:TwoHandedModularItem", TwoHandedModularItemBuilder.class, TwoHandedModularItemBuilder::new);
        RegistryInfo.ITEM.addType("TetraJS:DynamicModularItem", DynamicModularItemBuilder.class, DynamicModularItemBuilder::new);
        RegistryInfo.ITEM.addType("TetraJS:BowModularItem", BowModularItemBuilder.class, BowModularItemBuilder::new);
        RegistryInfo.ITEM.addType("TetraJS:ShieldModularItem", ShieldModularItemBuilder.class, ShieldModularItemBuilder::new);
        RegistryInfo.ITEM.addType("TetraJS:CrossBowModularItem", CrossBowModularItemBuilder.class, CrossBowModularItemBuilder::new);
        RegistryInfo.ITEM.addType("TetraJS:EquipModularItem", EquipModularItemBuilder.class, EquipModularItemBuilder::new);
        CompatManager.registerCompatItemBuilder(RegistryInfo.ITEM);
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.registerSimple(ItemEffect.class, TetraJSWrappers::toItemEffect);
        typeWrappers.registerSimple(ToolAction.class, TetraJSWrappers::toToolAction);
        typeWrappers.registerSimple(NbtSlotData.class, TetraJSWrappers::toNbtSlotData);
        typeWrappers.registerSimple(ItemAspect.class, TetraJSWrappers::toItemAspect);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("DynamicModularHelper", DynamicModularHelper.INSTANCE);
        event.add("StatBarHelper", StatBarHelper.INSTANCE);
        event.add("TetraDataManager", DataManager.instance);
        event.add("TetraSchematicRegistry", SchematicRegistry.class);
        event.add("TetraJSUtils", TetraJSUtils.INSTANCE);
        event.add("ShowModelHelper", ShowModelHelper.INSTANCE);
        addTetraJSBinding(event, "DynamicModularHelper", DynamicModularHelper.INSTANCE);
        addTetraJSBinding(event, "StatBarHelper", StatBarHelper.INSTANCE);
        addTetraJSBinding(event, "DataManager", DataManager.instance);
        addTetraJSBinding(event, "TetraJSUtils", TetraJSUtils.INSTANCE);
        addTetraJSBinding(event, "ShowModelHelper", ShowModelHelper.INSTANCE);
        addTetraJSBinding(event, "LangUtils", LangUtils.INSTANCE);
        addTetraJSBinding(event, "SchematicUtils", SchematicUtils.INSTANCE);
        CompatManager.registerCompatBindings((name, value) -> addTetraJSBinding(event, name, value));
    }

    public static void addTetraJSBinding(BindingsEvent event, String name, Object value) {
        event.add("TetraJS$" + name, value);
    }

    @Override
    public void registerEvents() {
        TetraJSEvents.GROUP.register();
    }
}
