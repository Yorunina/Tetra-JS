package net.yiran.tetrajs.core.mixins.tetrawear;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.yiran.tetrajs.kubejs.events.StealthVisibilityEventJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetrawear.systems.stealth.StealthSystem;
import se.mickelus.tetrawear.util.ArmorHelper;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = StealthSystem.class, remap = false)
public class StealthSystemMixin {
    @WrapOperation(
            method = "onLivingVisibility",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/entity/living/LivingEvent$LivingVisibilityEvent;modifyVisibility(D)V"
            )
    )
    private static void tetrajs$modifyVisibility(
            LivingEvent.LivingVisibilityEvent event,
            double visibility,
            Operation<Void> original,
            @Local(ordinal = 0) double baseModifier,
            @Local(ordinal = 1) double guiseModifier
    ) {
        if (event.getEntity().level().isClientSide()) {
            original.call(event, visibility);
            return;
        }
        LivingEntity sneaker = event.getEntity();
        StealthVisibilityEventJS jsEvent = new StealthVisibilityEventJS(
                sneaker,
                event.getLookingEntity(),
                baseModifier,
                guiseModifier,
                tetrajs$matchedGuises(event.getLookingEntity(), sneaker)
        );
        TetraJSEvents.StealthVisibility.post(jsEvent);
        original.call(event, jsEvent.getVisibility());
    }

    @Unique
    private static List<String> tetrajs$matchedGuises(Entity lookingEntity, LivingEntity sneaker) {
        List<String> matched = new ArrayList<>();
        if (!(lookingEntity instanceof LivingEntity lookingLiving)) {
            return matched;
        }
        EffectData effects = ArmorHelper.getArmorEffects(sneaker);
        if (effects.contains(ItemEffect.get("undeadStealth")) && MobType.UNDEAD.equals(lookingLiving.getMobType())) {
            matched.add("undeadStealth");
        }
        if (effects.contains(ItemEffect.get("arthropodStealth")) && MobType.ARTHROPOD.equals(lookingLiving.getMobType())) {
            matched.add("arthropodStealth");
        }
        if (effects.contains(ItemEffect.get("netherStealth")) && sneaker.level().getBiome(sneaker.blockPosition()).is(BiomeTags.IS_NETHER)) {
            matched.add("netherStealth");
        }
        return matched;
    }
}
