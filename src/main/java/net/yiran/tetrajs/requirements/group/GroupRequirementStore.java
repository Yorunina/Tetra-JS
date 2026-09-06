package net.yiran.tetrajs.requirements.group;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;

import java.util.HashMap;
import java.util.Map;

public class GroupRequirementStore {
    public String key;
    public String translation;
    public CraftingRequirement[] requirements;
    public boolean hideExtend = false;


    public static Map<String, GroupRequirementStore> groups = new HashMap<>();

    public static void handler(Map<ResourceLocation, GroupRequirementStore> data) {
        for (Map.Entry<ResourceLocation, GroupRequirementStore> entry : data.entrySet()) {
            store(entry.getKey(), entry.getValue());
        }
    }

    public static void store(ResourceLocation resourceLocation, GroupRequirementStore store) {
        String group = store.key == null ? resourceLocation.getPath() : store.key;
        if (store.translation == null) {
            store.translation = "tetrajs.group." + group;
        }
        groups.put(group, store);
    }
}
