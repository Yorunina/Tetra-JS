package net.yiran.tetrajs.sorter;


import com.google.gson.JsonElement;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;

import static se.mickelus.tetra.gui.stats.data.StatRegistry.gson;
import static se.mickelus.tetra.gui.stats.data.StatRegistry.registerStatGetter;

public class StatRegistry {
    public static void init() {
        registerStatGetter("tetrajs:item", StatRegistry::ItemGetter);
    }

    public static IStatGetter ItemGetter(JsonElement json){
        ItemData itemData = gson.fromJson(json, ItemData.class);
        return new StatGetterItem(itemData.stat, itemData.items, itemData.tag);
    }

    public static record ItemData(IStatGetter stat,String[] items,String tag){
    }
}
