package net.yiran.tetrajs.core.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.yiran.tetrajs.kubejs.events.HammerBlockCraftConsumeToolJS;
import net.yiran.tetrajs.kubejs.events.TetraJSEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.blocks.forged.hammer.HammerHeadBlock;

@Mixin(value = HammerHeadBlock.class, remap = false)
public class HammerHeadBlockMixin {
    @Inject(method = "onCraftConsumeTool", at = @At("HEAD"), cancellable = true)
    private void onCraftConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing, Player player, ToolAction requiredTool, int requiredLevel, boolean consumeResources, CallbackInfoReturnable<ItemStack> cir){
        if (world.isClientSide) return;
        if (TetraJSEvents.hammerBlockCraftConsumeTool.post(
                new HammerBlockCraftConsumeToolJS(world, pos, blockState, (HammerHeadBlock) (Object) this, targetStack, player, requiredTool, requiredLevel, consumeResources)).interruptFalse()) {
            cir.setReturnValue(targetStack);
        }
    }
}
