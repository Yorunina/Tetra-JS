package net.yiran.tetrajs.core.mixins;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.yiran.tetrajs.core.InfiniteImprovements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.properties.AttributeHelper;

import java.util.Arrays;
import java.util.Objects;

@Mixin(value = ItemModuleMajor.class, remap = false)
public abstract class ItemModuleMajorInfiniteMixin extends ItemModule {
    @Shadow
    protected ImprovementData[] improvements;

    public ItemModuleMajorInfiniteMixin(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);
    }

    @Shadow
    public abstract int getImprovementLevel(ItemStack itemStack, String improvementKey);

    /**
     * @author TetraJS
     * @reason Infinite improvements keep one definition while the stored level can grow.
     */
    @Overwrite
    public ImprovementData getImprovement(ItemStack itemStack, String improvementKey) {
        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getTag();
            return Arrays.stream(this.improvements)
                    .filter(improvement -> improvementKey.equals(improvement.key))
                    .filter(improvement -> tag.contains(this.slotTagKey + ":" + improvement.key))
                    .filter(improvement -> improvement.level == tag.getInt(this.slotTagKey + ":" + improvement.key) || InfiniteImprovements.isInfinite(improvement))
                    .findAny()
                    .orElse(null);
        }
        return null;
    }

    /**
     * @author TetraJS
     * @reason Infinite improvements keep one definition while the stored level can grow.
     */
    @Overwrite
    public ImprovementData[] getImprovements(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getTag();
            return Arrays.stream(this.improvements)
                    .filter(improvement -> tag.contains(this.slotTagKey + ":" + improvement.key))
                    .filter(improvement -> improvement.level == tag.getInt(this.slotTagKey + ":" + improvement.key) || InfiniteImprovements.isInfinite(improvement))
                    .toArray(ImprovementData[]::new);
        }
        return new ImprovementData[0];
    }

    /**
     * @author TetraJS
     * @reason Infinite improvements accept any stored level.
     */
    @Overwrite
    public boolean acceptsImprovementLevel(String improvementKey, int level) {
        return Arrays.stream(this.improvements)
                .filter(improvement -> improvementKey.equals(improvement.key))
                .anyMatch(improvement -> level == improvement.level || InfiniteImprovements.isInfinite(improvement));
    }


    @Inject(method = "lambda$removeCollidingImprovements$22", at = @At("HEAD"), cancellable = true)
    private static void tetrajs$matchInfiniteCollisions(int level, ImprovementData improvement, CallbackInfoReturnable<Boolean> cir) {
        if (InfiniteImprovements.isInfinite(improvement)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "addImprovement(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;I)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int tetrajs$stackInfiniteLevel(int level, ItemStack itemStack, String improvementKey) {
        ImprovementData improvementData = ((ItemModuleMajor) (Object) this).getImprovement(itemStack, improvementKey);
        if (InfiniteImprovements.isInfinite(improvementData)) {
            return this.getImprovementLevel(itemStack, improvementKey) + level;
        }
        return level;
    }

    /**
     * @author TetraJS
     * @reason Scale infinite improvement attributes by the stored level.
     */
    @Overwrite
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack itemStack) {
        return Arrays.stream(((ItemModuleMajor) (Object) this).getImprovements(itemStack))
                .map(improvement -> {
                    if (InfiniteImprovements.isInfinite(improvement) && improvement.attributes != null) {
                        int storedLevel = this.getImprovementLevel(itemStack, improvement.key);
                        Multimap<Attribute, AttributeModifier> scaled = ArrayListMultimap.create();
                        improvement.attributes.forEach((attribute, modifier) ->
                                scaled.put(attribute, new AttributeModifier(modifier.getId(), modifier.getName(), modifier.getAmount() * storedLevel, modifier.getOperation())));
                        return scaled;
                    }
                    return improvement.attributes;
                })
                .filter(Objects::nonNull)
                .reduce(super.getAttributeModifiers(itemStack), AttributeHelper::merge);
    }

    /**
     * @author TetraJS
     * @reason Scale infinite improvement properties by the stored level.
     */
    @Overwrite
    public ItemProperties getProperties(ItemStack itemStack) {
        return Arrays.stream(((ItemModuleMajor) (Object) this).getImprovements(itemStack))
                .map(improvement -> InfiniteImprovements.isInfinite(improvement)
                        ? improvement.multiply(this.getImprovementLevel(itemStack, improvement.key))
                        : improvement)
                .reduce(super.getProperties(itemStack), ItemProperties::merge, ItemProperties::merge);
    }

    /**
     * @author TetraJS
     * @reason Scale infinite improvement effects by the stored level.
     */
    @Overwrite
    public EffectData getEffectData(ItemStack itemStack) {
        return Arrays.stream(((ItemModuleMajor) (Object) this).getImprovements(itemStack))
                .map(improvement -> {
                    if (InfiniteImprovements.isInfinite(improvement)) {
                        float storedLevel = this.getImprovementLevel(itemStack, improvement.key);
                        return EffectData.multiply(improvement.effects, storedLevel, storedLevel);
                    }
                    return improvement.effects;
                })
                .filter(Objects::nonNull)
                .reduce(super.getEffectData(itemStack), EffectData::merge);
    }
}