package net.yiran.tetrajs.core.mixins;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mixin(value = ItemModularHandheld.class, remap = false)
public class ItemModularHandheldMixin {
    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<Attribute, AttributeModifier> tetrajs$uniqueOffhandAttributeIds(
            Multimap<Attribute, AttributeModifier> original,
            EquipmentSlot slot,
            ItemStack itemStack) {
        if (slot != EquipmentSlot.OFFHAND || original == null || original.isEmpty()) {
            return original;
        }
        ArrayListMultimap<Attribute, AttributeModifier> remapped = ArrayListMultimap.create();
        original.forEach((attribute, modifier) -> remapped.put(attribute, new AttributeModifier(
                UUID.nameUUIDFromBytes(("tetrajs:offhand:" + modifier.getId()).getBytes(StandardCharsets.UTF_8)),
                modifier.getName(),
                modifier.getAmount(),
                modifier.getOperation()
        )));
        return remapped;
    }
}