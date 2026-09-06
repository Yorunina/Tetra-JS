package net.yiran.tetrajs;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Boolean> CreateSingleCreativeTab;

    static {
        CreateSingleCreativeTab = BUILDER
                .define("CreateSingleCreativeTab", true);
        SPEC = BUILDER.build();
    }
}
