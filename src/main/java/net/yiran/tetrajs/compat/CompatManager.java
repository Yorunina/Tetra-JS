package net.yiran.tetrajs.compat;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.yiran.tetrajs.compat.chestcavity.ChestCavityCompat;
import net.yiran.tetrajs.compat.curios.CuriosHelper;
import net.yiran.tetrajs.compat.curios.builders.items.CuriosModularItemBuilder;
import net.yiran.tetrajs.compat.curios.builders.items.DynamicCuriosModularItemBuilder;
import net.yiran.tetrajs.compat.tetrawear.TetraWearHelper;
import top.theillusivec4.curios.Curios;

import java.util.function.BiConsumer;

public class CompatManager {
    public static boolean CuriosLoaded = ModList.get().isLoaded(Curios.MODID);
    public static boolean ChestCavityLoaded = ModList.get().isLoaded("chestcavity");

    public static void registerCompatItemBuilder(RegistryInfo<Item> registryInfo) {
        if (CuriosLoaded) {
            registryInfo.addType("TetraJS:CuriosModularItem", CuriosModularItemBuilder.class, CuriosModularItemBuilder::new);
            registryInfo.addType("TetraJS:DynamicCuriosModularItem", DynamicCuriosModularItemBuilder.class, DynamicCuriosModularItemBuilder::new);
        }
    }

    public static void registerCompatProviders() {
        if (ChestCavityLoaded) {
            ChestCavityCompat.register();
        }
    }

    public static void registerCompatBindings(BiConsumer<String, Object> register) {
        if (CuriosLoaded) {
            register.accept("CuriosHelper", CuriosHelper.getInstance());
        }
        if (ChestCavityLoaded) {
            ChestCavityCompat.registerBindings(register);
        }
        register.accept("TetraWearHelper", TetraWearHelper.INSTANCE);
    }
}
