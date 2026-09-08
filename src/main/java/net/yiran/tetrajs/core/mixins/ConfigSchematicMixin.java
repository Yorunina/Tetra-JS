package net.yiran.tetrajs.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.schematic.ConfigSchematic;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(value = ConfigSchematic.class, remap = false)
public class ConfigSchematicMixin {
    @WrapOperation(method = "applyOutcome", at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/module/schematic/ConfigSchematic;removePreviousModule(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)Lse/mickelus/tetra/module/ItemModule;"), remap = false)
    private ItemModule removePreviousModule(ConfigSchematic instance, ItemStack itemStack, String slot, Operation<ItemModule> original, @Local(name = "module") ItemModule itemModule) {
        IModularItem item = (IModularItem) itemStack.getItem();
        ItemModule previousModule = item.getModuleFromSlot(itemStack, slot);
        if (previousModule != null) {
            return previousModule.equals(itemModule) ? itemModule : original.call(instance, itemStack, slot);
        } else {
            return null;
        }
    }

    @WrapOperation(
            method = "getPreviews",
            at = @At(value = "INVOKE", target = "Lse/mickelus/mutil/util/Filter;distinct(Ljava/util/function/Function;)Ljava/util/function/Predicate;")
    )
    private Predicate<OutcomePreview> tetrajs$skipNullPreviews(Function<OutcomePreview, ?> keyExtractor, Operation<Predicate<OutcomePreview>> original) {
        Predicate<OutcomePreview> distinct = original.call(keyExtractor);
        return preview -> preview != null && distinct.test(preview);
    }
}