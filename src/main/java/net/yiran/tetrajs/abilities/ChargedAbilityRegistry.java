package net.yiran.tetrajs.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ChargedAbilityRegistry {
    private static final List<KubeChargedAbilityEffect> abilities = new ArrayList<>();

    private ChargedAbilityRegistry() {
    }

    public static void register(List<KubeChargedAbilityEffect> definitions) {
        abilities.clear();
        abilities.addAll(definitions);
        abilities.sort(Comparator.comparingInt(KubeChargedAbilityEffect::getPriority).reversed());
    }

    public static ChargedAbilityEffect findAvailable(ItemModularHandheld item, ItemStack stack) {
        return abilities.stream()
                .filter(ability -> ability.isAvailable(item, stack))
                .findFirst()
                .orElse(null);
    }

    public static boolean hasAvailable(ItemModularHandheld item, ItemStack stack) {
        return findAvailable(item, stack) != null;
    }

    public static ChargedAbilityEffect findRiptideOverride(ItemModularHandheld item, ItemStack stack) {
        return abilities.stream()
                .filter(KubeChargedAbilityEffect::overridesRiptide)
                .filter(ability -> ability.isAvailable(item, stack))
                .findFirst()
                .orElse(null);
    }

    public static boolean perform(Player player, InteractionHand hand, LivingEntity target, BlockPos block,
                                  Vec3 hit, int chargedTicks) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ItemModularHandheld item)) {
            return false;
        }
        KubeChargedAbilityEffect ability = abilities.stream()
                .filter(entry -> entry.canPerform(player, item, stack, target, block, chargedTicks))
                .findFirst()
                .orElse(null);
        if (ability == null) {
            return false;
        }
        ability.perform(player, hand, item, stack, target, block, hit, chargedTicks);
        player.stopUsingItem();
        return true;
    }
}
