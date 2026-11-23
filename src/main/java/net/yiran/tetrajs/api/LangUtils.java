package net.yiran.tetrajs.api;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.language.I18n;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.data.MaterialData;

public class LangUtils {
    public static LangUtils INSTANCE = new LangUtils();

    public JsonObject getLackMaterialLang() {
        var json = new JsonObject();
        return addLackMaterialLang(json);
    }

    public JsonObject getEmptyObject() {
        return new JsonObject();
    }

    public JsonObject addLackMaterialLang(JsonObject json) {
        for (MaterialData materialData : DataManager.instance.materialData.getData().values()) {
            var key = materialData.key;
            var items = materialData.material.getApplicableItemStacks();
            if (items.length > 0) {
                var item = items[0];
                if (!I18n.exists("tetra.material." + key)) {
                    json.addProperty("tetra.material." + key, I18n.get(item.getDescriptionId()));
                }
                if (!I18n.exists("tetra.material." + key + ".prefix")) {
                    json.addProperty("tetra.material." + key + ".prefix", I18n.get(item.getDescriptionId()));
                }
            }
        }
        return json;
    }

    public JsonObject addSchematic(JsonObject json, String path, String name, String description, String... slots) {
        json.addProperty("tetra/schematic/" + path + ".name", name);
        json.addProperty("tetra/schematic/" + path + ".description", description);
        int index = 0;
        for (String slot : slots) {
            json.addProperty("tetra/schematic/" + path + ".slot"+ ++index, slot);
        }
        return json;
    }
    public JsonObject addEffect(JsonObject json, ItemEffect effect, String name, String tooltip, String tooltip_short,String tooltip_extended) {
        json.addProperty("tetra.stats." + effect.getKey(), name);
        if(tooltip != null) {
            json.addProperty("tetra.stats." + effect.getKey() + ".tooltip", tooltip);
        }
        if (tooltip_short!=null) {
            json.addProperty("tetra.stats." + effect.getKey() + ".tooltip_short", tooltip_short);
        }
        if (tooltip_extended!=null) {
            json.addProperty("tetra.stats." + effect.getKey() + ".tooltip_extended", tooltip_extended);
        }
        return json;
    }
}
