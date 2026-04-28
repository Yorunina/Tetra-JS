package net.yiran.tetrajs.api;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.model.IModuleModel;
import se.mickelus.tetra.module.data.SynergyData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShowModelHelper {
    public static ShowModelHelper INSTANCE = new ShowModelHelper();
    public static Codec<List<String>> CODEC = Codec.STRING.listOf();

    public void addShowModel(ItemStack stack, String... type) {
        var list = getShowModels(stack);
        list.addAll(List.of(type));
        saveShowModels(stack, list);
    }

    public void removeShowModel(ItemStack stack, String... type) {
        var list = getShowModels(stack);
        List.of(type).forEach(list::remove);
        saveShowModels(stack, list);
    }

    public boolean hasShowModel(ItemStack stack, String type) {
        var list = getShowModels(stack);
        return list.contains(type);
    }

    public void clearShowModel(ItemStack stack) {
        saveShowModels(stack, new HashSet<>());
    }

    public Set<String> getShowModels(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        var list = tag.getList("showModels", Tag.TAG_STRING);
        return new HashSet<>(CODEC.parse(NbtOps.INSTANCE, list)
                .result()
                .orElse(List.of()));
    }

    public void saveShowModels(ItemStack stack, Set<String> slots) {
        var tag = stack.getOrCreateTag();
        tag.put("showModels", CODEC.encodeStart(NbtOps.INSTANCE, slots.stream().toList()).result().get());
        IModularItem.updateIdentifier(tag);
    }

    @OnlyIn(Dist.CLIENT)
    public static ImmutableList<IModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity,
                                                       Function<ItemStack, SynergyData[]> getSynergyData, Function<ItemStack, Collection<ItemModule>> getAllModules,
                                                       String... defaultShowModels
    ) {
        List<String> showModelTypes = new ArrayList<>();
        if (itemStack.hasTag()) {
            var tag = itemStack.getTag().getList("showModels", Tag.TAG_STRING);
            showModelTypes.addAll(CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(List.of()));
        }
        showModelTypes.addAll(List.of(defaultShowModels));
        return Stream.concat(
                        Arrays.stream(getSynergyData.apply(itemStack))
                                .flatMap(synergyData -> Arrays.stream(synergyData.models)),
                        getAllModules.apply(itemStack).stream()
                                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                                .flatMap(itemModule -> Arrays.stream(itemModule.getModels(itemStack)))
                )
                .filter(Objects::nonNull)
                .filter(moduleModel -> showModelTypes.isEmpty() || showModelTypes.contains(moduleModel.getType().toString()))
                .sorted(Comparator.comparing(IModuleModel::getRenderLayer))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }
}
