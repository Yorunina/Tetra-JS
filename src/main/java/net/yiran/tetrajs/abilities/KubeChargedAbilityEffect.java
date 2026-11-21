package net.yiran.tetrajs.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import java.util.Locale;

public class KubeChargedAbilityEffect extends ChargedAbilityEffect {
    private final String key;
    private int priority;
    private AvailabilityFunction availability;
    private PerformFunction perform;

    public KubeChargedAbilityEffect(String effect) {
        super(0, 0, 0, 0, ItemEffect.get(effect), TargetRequirement.none, UseAnim.NONE);
        this.key = effect;
    }

    public KubeChargedAbilityEffect setChargeTime(int chargeTime) {
        this.chargeTimeFlat = chargeTime;
        return this;
    }

    public KubeChargedAbilityEffect setChargeTimeMultiplier(double multiplier) {
        this.chargeTimeSpeedMultiplier = multiplier;
        return this;
    }

    public KubeChargedAbilityEffect setCooldown(int cooldown) {
        this.cooldownFlat = cooldown;
        return this;
    }

    public KubeChargedAbilityEffect setCooldownMultiplier(double multiplier) {
        this.cooldownSpeedMultiplier = multiplier;
        return this;
    }

    public KubeChargedAbilityEffect setTarget(String target) {
        this.target = TargetRequirement.valueOf(target.toLowerCase(Locale.ROOT));
        return this;
    }

    public KubeChargedAbilityEffect setPose(String pose) {
        this.useAction = UseAnim.valueOf(pose.toUpperCase(Locale.ROOT));
        return this;
    }

    public KubeChargedAbilityEffect setModelTransform(String modelTransform) {
        this.modelTransform = modelTransform;
        return this;
    }

    public KubeChargedAbilityEffect setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public KubeChargedAbilityEffect availableWhen(AvailabilityFunction availability) {
        this.availability = availability;
        return this;
    }

    public KubeChargedAbilityEffect onPerform(PerformFunction perform) {
        this.perform = perform;
        return this;
    }

    public String getKey() {
        return key;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isAvailable(ItemModularHandheld item, ItemStack stack) {
        return super.isAvailable(item, stack) && (availability == null || availability.test(item, stack));
    }

    private void run(Player player, InteractionHand hand, ItemModularHandheld item, ItemStack stack,
                     LivingEntity target, BlockPos block, Vec3 hit, int chargedTicks) {
        if (player.level().isClientSide || perform == null) {
            return;
        }
        perform.accept(new Context(player, hand, item, stack, target, block, hit, chargedTicks));
        player.getCooldowns().addCooldown(item, getCooldown(item, stack, player));
    }

    @Override
    public void perform(Player player, InteractionHand hand, ItemModularHandheld item, ItemStack stack,
                        LivingEntity target, BlockPos block, Vec3 hit, int chargedTicks) {
        run(player, hand, item, stack, target, block, hit, chargedTicks);
    }

    @Override
    public void perform(Player player, InteractionHand hand, ItemModularHandheld item, ItemStack stack,
                        LivingEntity target, Vec3 hit, int chargedTicks) {
        run(player, hand, item, stack, target, null, hit, chargedTicks);
    }

    @Override
    public void perform(Player player, InteractionHand hand, ItemModularHandheld item, ItemStack stack,
                        BlockPos block, Vec3 hit, int chargedTicks) {
        run(player, hand, item, stack, null, block, hit, chargedTicks);
    }

    @Override
    public void perform(Player player, InteractionHand hand, ItemModularHandheld item, ItemStack stack,
                        int chargedTicks) {
        run(player, hand, item, stack, null, null, null, chargedTicks);
    }

    @FunctionalInterface
    public interface AvailabilityFunction {
        boolean test(ItemModularHandheld item, ItemStack stack);
    }

    @FunctionalInterface
    public interface PerformFunction {
        void accept(Context context);
    }

    public record Context(Player player, InteractionHand hand, ItemModularHandheld item, ItemStack stack,
                          LivingEntity target, BlockPos block, Vec3 hit, int chargedTicks) {
    }
}
