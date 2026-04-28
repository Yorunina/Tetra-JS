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
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItemImpl;

import java.util.function.Function;

@Mixin(ModularCrossbowItemImpl.class)
public class ModularCrossbowItemMixin {
    @Inject(method = "fireProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setSoundEvent(Lnet/minecraft/sounds/SoundEvent;)V"))
    private void onFireProjectile(Level world, ItemStack crossbowStack, ItemStack ammoStack, ImmutableList<Function<AbstractArrow, AbstractArrow>> projectileRemappers,
                                         Player player, double strength, float projectileVelocity, float pitch, float yaw, boolean isDupe, CallbackInfo ci,
                                         @Local(name = "projectile") LocalRef<AbstractArrow> projectile, @Local(name = "ammoItem") ArrowItem ammoItem) {
        CreateArrowEventJS event = new CreateArrowEventJS(crossbowStack, world, ammoItem, ammoStack, player, projectile.get());
        TetraJSEvents.CreateArrow.post(event, "crossbow");
        if (event.isModifyProjectile()) {
            projectile.set(event.getProjectile());
        }
    }
}
