package net.yiran.tetrajs.core.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Mixin(ItemModuleMajor.class)
public class ItemModuleMajorMixin {

    @Redirect(
            method = {
                    "getEnchantments(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Map;", "getEnchantmentsPrimitive(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Map;"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Collectors;toMap(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/util/stream/Collector;"
            ),
            remap = false
    )
    private Collector<Object, ?, Map<Object, Object>> tetrajs$mergeDuplicateEnchantments(
            Function<Object, Object> keyMapper,
            Function<Object, Object> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, (first, second) -> Math.max((Integer) first, (Integer) second));
    }
}
