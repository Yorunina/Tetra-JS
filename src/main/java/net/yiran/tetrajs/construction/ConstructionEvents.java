package net.yiran.tetrajs.construction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yiran.tetrajs.TetraJS;

@Mod.EventBusSubscriber(modid = TetraJS.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConstructionEvents {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        if (player == null || player.isSpectator()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!ConstructionJob.hasEffect(stack)) {
            return;
        }
        BlockHitResult hit = event.getHitVec();
        if (hit == null) {
            return;
        }
        if (player.isCrouching() && ConstructionUndoHistory.isUndoActive(player)) {
            if (ConstructionUndoHistory.undo(player, event.getLevel(), hit.getBlockPos())) {
                deny(event, player, true);
                return;
            }
        }
        if (ConstructionJob.wouldPerformToolAction(player, event.getLevel(), event.getHand(), hit, stack)) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            if (!ConstructionJob.collectPositions(player, event.getLevel(), event.getHand(), hit, stack).isEmpty()) {
                deny(event, player, false);
            }
            return;
        }
        if (ConstructionJob.execute(player, event.getLevel(), event.getHand(), hit, stack)) {
            deny(event, player, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        if (player == null || !player.isCrouching() || !ConstructionJob.hasEffect(event.getItemStack())) {
            return;
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void deny(PlayerInteractEvent.RightClickBlock event, Player player, boolean swing) {
        if (swing) {
            player.swing(event.getHand(), true);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);
    }
}
