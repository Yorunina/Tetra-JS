package net.yiran.tetrajs.items;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yiran.tetrajs.TetraJS;
import net.yiran.tetrajs.api.ShowModelHelper;
import net.yiran.tetrajs.util.NbtSlotData;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.model.IModuleModel;
import se.mickelus.tetra.module.data.SynergyData;
import se.mickelus.tetra.module.schematic.RepairSchematic;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DynamicModularItem extends ItemModularHandheld {
    public Cache<String, List<NbtSlotData>> slotCache;
    public Cache<String, List<String>> showModuleCache;
    public String[] synergiesPath;

    public DynamicModularItem(ResourceLocation itemID, boolean canHone, int honeBase, int honeIntegrityMultiplier, String[] synergiesPath) {
        super(new Properties().stacksTo(1));
        slotCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(5L, TimeUnit.MINUTES).build();
        showModuleCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(5L, TimeUnit.MINUTES).build();
        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, itemID.getPath()));
        this.canHone = canHone;
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
        this.synergiesPath = synergiesPath;
        TetraJS.items.add(this);
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        DataManager.instance.synergyData.onReload(() -> {
            this.synergies = DataManager.instance.synergyData
                    .getData()
                    .entrySet()
                    .stream()
                    .filter((entry) -> entry.getKey().getNamespace().equals("tetra"))
                    .filter(entry -> Arrays.stream(synergiesPath).anyMatch(s -> entry.getKey().getPath().startsWith(s)))
                    .map(Map.Entry::getValue)
                    .flatMap(Arrays::stream)
                    .toArray(SynergyData[]::new);
        });
    }

    public List<NbtSlotData> getSlotData(ItemStack itemStack) {
        var id = this.getIdentifier(itemStack);
        if (id == null || id.isEmpty()) return List.of();
        try {
            return slotCache.get(id, () -> {
                var tag = itemStack.getTag().getList("extraslots", Tag.TAG_COMPOUND);
                return NbtSlotData.LCODEC.parse(NbtOps.INSTANCE, tag).result().orElse(List.of());
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        var pos = getSlotData(itemStack)
                .stream()
                .filter(NbtSlotData::isMinor)
                .flatMapToInt(NbtSlotData::getPos)
                .toArray();
        return new GuiModuleOffsets(pos);
    }

    @Override
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        var pos = getSlotData(itemStack)
                .stream()
                .filter(NbtSlotData::isMajor)
                .flatMapToInt(NbtSlotData::getPos)
                .toArray();
        return new GuiModuleOffsets(pos);
    }

    @Override
    public String[] getMinorModuleKeys(ItemStack itemStack) {
        return getSlotData(itemStack)
                .stream()
                .filter(NbtSlotData::isMinor)
                .map(NbtSlotData::slot)
                .toArray(String[]::new);
    }

    @Override
    public String[] getMajorModuleKeys(ItemStack itemStack) {
        return getSlotData(itemStack)
                .stream()
                .filter(NbtSlotData::isMajor)
                .map(NbtSlotData::slot)
                .toArray(String[]::new);
    }

    @Override
    public String[] getRequiredModules(ItemStack itemStack) {
        return getSlotData(itemStack)
                .stream()
                .filter(NbtSlotData::required)
                .map(NbtSlotData::slot)
                .toArray(String[]::new);
    }

    @OnlyIn(Dist.CLIENT)
    public ImmutableList<IModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        return ShowModelHelper.getModels(itemStack, entity, this::getSynergyData, this::getAllModules);
    }

}
