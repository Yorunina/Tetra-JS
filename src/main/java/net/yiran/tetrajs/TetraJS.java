package net.yiran.tetrajs;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.yiran.tetrajs.crafteffect.CustomCraftingEffectOutcome;
import net.yiran.tetrajs.data.RequirementDataManager;
import net.yiran.tetrajs.data.RequirementUpdateDataPacket;
import net.yiran.tetrajs.construction.ConstructionNetwork;
import net.yiran.tetrajs.kubejs.events.EnchantAspectRegisterEventJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import net.yiran.tetrajs.probejs.TetraProbePlugin;
import net.yiran.tetrajs.requirements.*;
import net.yiran.tetrajs.requirements.group.GroupRequirement;
import net.yiran.tetrajs.sorter.StatRegistry;
import org.slf4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.InitializableItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItemImpl;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirementDeserializer;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.util.ArrayList;
import java.util.List;

@Mod(TetraJS.MODID)
@SuppressWarnings("removal")
public class TetraJS {
    public static final String MODID = "tetrajs";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static List<InitializableItem> items = new ArrayList<>();
    public static IEventBus ModEventBus;
    public static PacketHandler NETWORK;

    public TetraJS() {
        if (ModList.get().isLoaded("morerequirement")) {
            throw new IllegalStateException("Tetra-JS now includes MoreRequirement. Remove the MoreRequirement jar.");
        }
        if (ModList.get().isLoaded(ProbeJS.MOD_ID)) {
            ProbeJSPlugins.register(new TetraProbePlugin());
        }
        ModEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        NETWORK = new PacketHandler(MODID, "requirement", "1");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModEventBus.addListener(TetraJSClient::onClientSetup);
            StatRegistry.init();
        }
        ModEventBus.addListener(EventPriority.LOWEST, this::onCommonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(RequirementDataManager.instance);
        CreativeTabHandler.init();
    }

    public void onCommonSetup(final FMLCommonSetupEvent event) {
        NETWORK.registerPacket(RequirementUpdateDataPacket.class, RequirementUpdateDataPacket::new);
        ConstructionNetwork.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            TetraJSClient.itemClientInit(items);
        }
        items.forEach(init -> init.commonInit(TetraMod.packetHandler));
        items.clear();
        items = null;
        TetraJSEvents.EnchantAspectRegister.post(new EnchantAspectRegisterEventJS());
        SchematicRegistry.instance.registerSchematic(new RepairSchematic(ModularShieldItem.instance, "modular_shield"));
        SchematicRegistry.instance.registerSchematic(new RepairSchematic(ModularBowItem.instance, "modular_bow"));
        SchematicRegistry.instance.registerSchematic(new RepairSchematic(ModularCrossbowItemImpl.instance, "modular_crossbow"));

    }

    public static void registerRequirements() {
        registerBoth("tetrajs:advancement", AdvancementRequirement.class);
        registerBoth("tetrajs:all_improvement", AllImprovementRequirement.class);
        registerBoth("tetrajs:biome", BiomeRequirement.class);
        registerBoth("tetrajs:custom", CustomRequirement.class);
        registerBoth("tetrajs:dimension", DimensionRequirement.class);
        registerBoth("tetrajs:entities", EntitiesRequirement.class);
        registerBoth("tetrajs:height", HeightRequirement.class);
        registerBoth("tetrajs:moon_phase", MoonPhaseRequirement.class);
        registerBoth("tetrajs:mbd", MultiblockRequirement.class);
        registerBoth("tetrajs:potion", PotionEffectRequirement.class);
        registerBoth("tetrajs:see_sky", SeeSkyRequirement.class);
        registerBoth("tetrajs:time", TimeRequirement.class);
        registerBoth("tetrajs:weather", WeatherRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetrajs:group", GroupRequirement.class);
        CraftingRequirementDeserializer.registerSupplier("tetrajs:other_module", OtherModuleRequirement.class);
        CraftingEffectRegistry.registerConditionType("tetrajs:wrap_target", WarpTargetItemRequirement.class);
        CraftingEffectRegistry.registerEffectType("tetrajs:custom", CustomCraftingEffectOutcome.class);
    }

    private static <T extends CraftingRequirement & CraftingEffectCondition> void registerBoth(String id, Class<T> type) {
        CraftingRequirementDeserializer.registerSupplier(id, type);
        CraftingEffectRegistry.registerConditionType(id, type);
    }
}
