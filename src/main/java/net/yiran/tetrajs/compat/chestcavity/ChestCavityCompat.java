package net.yiran.tetrajs.compat.chestcavity;

import net.tigereye.chestcavity.interfaces.OrganDataProviders;

import java.util.function.BiConsumer;

public final class ChestCavityCompat {
    private static final TetraOrganDataProvider PROVIDER = new TetraOrganDataProvider();

    private ChestCavityCompat() {
    }

    public static void register() {
        OrganDataProviders.register(PROVIDER);
    }

    public static void registerBindings(BiConsumer<String, Object> register) {
        register.accept("TetraOrganRegistry", TetraOrganRegistry.class);
    }
}
