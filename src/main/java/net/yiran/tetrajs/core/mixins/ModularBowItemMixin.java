package net.yiran.tetrajs.core.mixins;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yiran.tetrajs.kubejs.events.CreateArrowEventJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;

import java.util.function.Function;

@Mixin(value = ModularBowItem.class, remap = false)
public class ModularBowItemMixin {
    @Inject(method = "fireProjectile", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;iterator()Lcom/google/common/collect/UnmodifiableIterator;"))
    private static void onFireProjectile(ItemStack itemStack, Level world, ArrowItem ammoItem, ItemStack ammoStack, ImmutableList<Function<AbstractArrow, AbstractArrow>> projectileRemappers,
                                         Player player, float basePitch, float yaw, float projectileVelocity, float accuracy,
                                         int drawProgress, double strength, int powerLevel, int punchLevel, int flameLevel,
                                         int piercingLevel, boolean hasSuspend, boolean infiniteAmmo, CallbackInfo ci, @Local(name = "projectile") LocalRef<AbstractArrow> projectile) {
        CreateArrowEventJS event = new CreateArrowEventJS(itemStack, world, ammoItem, ammoStack, player, projectile.get(), drawProgress);
        TetraJSEvents.CreateArrow.post(event, "bow");
        if (event.isModifyProjectile()) {
            projectile.set(event.getProjectile());
        }
    }
}
