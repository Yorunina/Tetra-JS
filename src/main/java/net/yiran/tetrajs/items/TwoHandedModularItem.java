package net.yiran.tetrajs.items;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yiran.tetrajs.TetraJS;
import net.yiran.tetrajs.api.ShowModelHelper;
import net.yiran.tetrajs.util.SlotData;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.model.IModuleModel;
import se.mickelus.tetra.module.data.SynergyData;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.AttributeHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TwoHandedModularItem extends ItemModularHandheld implements ITetraJSItem {
    public GuiModuleOffsets majorOffsets;
    public GuiModuleOffsets minorOffsets;
    public String[] synergiesPath;
    public Cache<String, List<String>> showModuleCache;


    public TwoHandedModularItem(ResourceLocation itemID, List<SlotData> majorsData, List<SlotData> minorsData, boolean canHone, int honeBase, int honeIntegrityMultiplier, String[] synergiesPath) {
        super(new Properties().stacksTo(1));
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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack itemStack) {
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            return this.getAttributeModifiersCached(itemStack);
        }
        return AttributeHelper.emptyMap;
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        synergyDataSync();
    }

    @Override
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return this.minorOffsets;
    }

    @Override
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return this.majorOffsets;
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
    public void setSynergies(SynergyData[] synergyData) {
        this.synergies = synergyData;
    }

    @Override
    public String[] getSynergiesPath() {
        return synergiesPath;
    }

     @OnlyIn(Dist.CLIENT)
    public ImmutableList<IModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        return ShowModelHelper.getModels(itemStack, entity, this::getSynergyData, this::getAllModules);
    }

}
