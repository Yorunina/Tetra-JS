package net.yiran.tetrajs.items;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yiran.tetrajs.TetraJS;
import net.yiran.tetrajs.api.ShowModelHelper;
import net.yiran.tetrajs.util.SlotData;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.model.IModuleModel;
import se.mickelus.tetra.module.data.SynergyData;
import se.mickelus.tetra.module.schematic.RepairSchematic;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BowModularItem extends ModularBowItem implements ITetraJSItem {
    public GuiModuleOffsets majorOffsets;
    public GuiModuleOffsets minorOffsets;
    public String[] synergiesPath;
    public Cache<String, List<String>> showModuleCache;

    public BowModularItem(ResourceLocation itemID, List<SlotData> majorsData, List<SlotData> minorsData, boolean canHone, int honeBase, int honeIntegrityMultiplier, String[] synergiesPath) {
        slotInit(majorsData, minorsData);
        showModuleCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(5L, TimeUnit.MINUTES).build();
        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, itemID.getPath()));
        this.canHone = canHone;
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
        this.synergiesPath = synergiesPath;
        TetraJS.items.add(this);
    }

    @Override
    public void setMajorOffsets(GuiModuleOffsets majorOffsets) {
        this.majorOffsets = majorOffsets;
    }

    @Override
    public void setMinorOffsets(GuiModuleOffsets minorOffsets) {
        this.minorOffsets = minorOffsets;
    }

    @Override
    public void setMajorModuleKeys(String[] majorModuleKeys) {
        this.majorModuleKeys = majorModuleKeys;
    }

    @Override
    public void setMinorModuleKeys(String[] minorModuleKeys) {
        this.minorModuleKeys = minorModuleKeys;
    }

    @Override
    public void setRequiredModules(String[] requiredModules) {
        this.requiredModules = requiredModules;
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        synergyDataSync();
    }

    @Override
    public void setSynergies(SynergyData[] synergyData) {
        this.synergies = synergyData;
    }

    @Override
    public String[] getSynergiesPath() {
        return synergiesPath;
    }

    public String getDrawVariant(ItemStack itemStack, @Nullable LivingEntity entity) {
        float progress = this.getProgress(itemStack, entity);
        if (progress == 0.0F) {
            return "item";
        } else if ((double)progress < 0.65) {
            return "draw_0";
        } else {
            return (double)progress < 0.9 ? "draw_1" : "draw_2";
        }
    }

     @OnlyIn(Dist.CLIENT)
    public ImmutableList<IModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        return ShowModelHelper.getModels(itemStack, entity, this::getSynergyData, this::getAllModules,this.getDrawVariant(itemStack, entity));
    }

}
