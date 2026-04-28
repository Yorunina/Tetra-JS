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
import net.yiran.tetrajs.compat.CompatManager;
import net.yiran.tetrajs.kubejs.events.EnchantAspectRegisterEventJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import net.yiran.tetrajs.probejs.TetraProbePlugin;
import org.slf4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.InitializableItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItemImpl;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RepairSchematic;
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

    public TetraJS() {
        if (ModList.get().isLoaded(ProbeJS.MOD_ID)) {
            ProbeJSPlugins.register(new TetraProbePlugin());
        }
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(TetraJSClient::onClientSetup);
        }
        modEventBus.addListener(EventPriority.LOWEST, this::onCommonSetup);
    }

    public void onCommonSetup(final FMLCommonSetupEvent event) {
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

}
