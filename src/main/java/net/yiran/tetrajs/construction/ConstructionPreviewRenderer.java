package net.yiran.tetrajs.construction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yiran.tetrajs.TetraJS;

import java.util.List;
import java.util.Collection;

@Mod.EventBusSubscriber(modid = TetraJS.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ConstructionPreviewRenderer {
    private static BlockPos lastPos;
    private static Direction lastDir;
    private static String lastFingerprint;
    private static List<BlockPos> lastPositions = List.of();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!ConstructionJob.hasEffect(stack)) {
            return;
        }
        BlockHitResult hit = (BlockHitResult) minecraft.hitResult;
        if (player.isCrouching()) {
            renderBoxes(event, minecraft, ConstructionClientEvents.undoBlocks(), 0F, 1F, 0F);
        } else {
            if (ConstructionJob.wouldPerformToolAction(player, minecraft.level, InteractionHand.MAIN_HAND, hit, stack)) {
                return;
            }
            renderBoxes(event, minecraft, cachedPositions(player, minecraft, hit, stack), 0F, 0F, 0F);
        }
    }

    private static void renderBoxes(RenderLevelStageEvent event, Minecraft minecraft, Collection<BlockPos> positions, float red, float green, float blue) {
        if (positions.isEmpty()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer consumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        for (BlockPos pos : positions) {
            LevelRenderer.renderLineBox(
                    poseStack,
                    consumer,
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                    red, green, blue, 0.4F
            );
        }
        minecraft.renderBuffers().bufferSource().endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static List<BlockPos> cachedPositions(Player player, Minecraft minecraft, BlockHitResult hit, ItemStack stack) {
        String fingerprint = new ConstructionOptions(stack).fingerprint();
        if (lastPos != null
                && lastPos.equals(hit.getBlockPos())
                && lastDir == hit.getDirection()
                && fingerprint.equals(lastFingerprint)
                && lastPositions.size() >= 2) {
            return lastPositions;
        }
        lastPos = hit.getBlockPos();
        lastDir = hit.getDirection();
        lastFingerprint = fingerprint;
        lastPositions = ConstructionJob.collectPositions(player, minecraft.level, InteractionHand.MAIN_HAND, hit, stack);
        return lastPositions;
    }
}
