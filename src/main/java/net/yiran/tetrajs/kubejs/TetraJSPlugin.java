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
import net.yiran.tetrajs.crafteffect.CustomCraftingEffectOutcome;
import net.yiran.tetrajs.requirements.CustomRequirement;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
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
        event.add("TetraDynamicModularHelper", DynamicModularHelper.INSTANCE);
        event.add("TetraStatBarHelper", StatBarHelper.INSTANCE);
        event.add("TetraDataManager", DataManager.instance);
        event.add("TetraShowModelHelper", ShowModelHelper.INSTANCE);
        event.add("TetraLangUtils", LangUtils.INSTANCE);
        event.add("TetraSchematicUtils", SchematicUtils.INSTANCE);
        event.add("TetraItemModularHandheld", ItemModularHandheld.class);
        event.add("TetraCustomRequirement", CustomRequirement.class);
        event.add("TetraCustomCraftingEffectCondition", CustomRequirement.Condition.class);
        event.add("TetraCustomCraftingEffect", CustomCraftingEffectOutcome.class);
        CompatManager.registerCompatBindings(event::add);
    }


    @Override
    public void registerEvents() {
        TetraJSEvents.GROUP.register();
    }
}
