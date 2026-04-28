package net.yiran.tetrajs.compat;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.yiran.tetrajs.compat.curios.CuriosHelper;
import net.yiran.tetrajs.compat.curios.builders.items.CuriosModularItemBuilder;
import net.yiran.tetrajs.compat.curios.builders.items.DynamicCuriosModularItemBuilder;
import top.theillusivec4.curios.Curios;

import java.util.function.BiConsumer;

public class CompatManager {
    public static boolean CuriosLoaded = ModList.get().isLoaded(Curios.MODID);

    public static void registerCompatItemBuilder(RegistryInfo<Item> registryInfo) {
        if (CuriosLoaded) {
            registryInfo.addType("TetraJS:CuriosModularItem", CuriosModularItemBuilder.class, CuriosModularItemBuilder::new);
            registryInfo.addType("TetraJS:DynamicCuriosModularItem", DynamicCuriosModularItemBuilder.class, DynamicCuriosModularItemBuilder::new);
        }
    }

    public static void registerCompatBindings(BiConsumer<String, Object> register) {
        if (CuriosLoaded) {
            register.accept("CuriosHelper", CuriosHelper.getInstance());
        }
    }
}
