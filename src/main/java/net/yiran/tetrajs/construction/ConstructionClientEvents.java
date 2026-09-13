package net.yiran.tetrajs.construction;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yiran.tetrajs.TetraJS;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = TetraJS.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ConstructionClientEvents {
    private static final Set<BlockPos> UNDO_BLOCKS = new HashSet<>();
    private static boolean lastCrouch;
    private static int selectedIndex;

    public static void setUndoBlocks(Set<BlockPos> positions) {
        UNDO_BLOCKS.clear();
        UNDO_BLOCKS.addAll(positions);
    }

    public static Set<BlockPos> undoBlocks() {
        return Collections.unmodifiableSet(UNDO_BLOCKS);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            lastCrouch = false;
            return;
        }
        boolean crouching = player.isCrouching() && ConstructionJob.hasEffect(player.getMainHandItem());
        if (crouching != lastCrouch) {
            lastCrouch = crouching;
            ConstructionNetwork.sendQueryUndo(crouching);
            if (!crouching) {
                UNDO_BLOCKS.clear();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !player.isCrouching() || event.getScrollDelta() == 0 || isUltimineKeyDown()) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!ConstructionJob.hasEffect(stack)) {
            return;
        }
        int delta = event.getScrollDelta() < 0 ? 1 : -1;
        selectedIndex = Math.floorMod(selectedIndex + delta, ConstructionOptions.KEYS.length);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        if (player == null || !player.isCrouching() || !ConstructionJob.hasEffect(event.getItemStack())) {
            return;
        }
        HitResult hit = Minecraft.getInstance().hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            return;
        }
        ConstructionOptions options = new ConstructionOptions(event.getItemStack());
        String key = ConstructionOptions.KEYS[selectedIndex];
        options.next(key);
        ConstructionNetwork.sendOption(key, options.getValueString(key));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !player.isCrouching() || isUltimineKeyDown() || !ConstructionJob.hasEffect(player.getMainHandItem())) {
            return;
        }
        ConstructionOptions options = new ConstructionOptions(player.getMainHandItem());
        Font font = minecraft.font;
        GuiGraphics graphics = event.getGuiGraphics();
        int x = 2;
        int y = 2;
        for (int i = 0; i < ConstructionOptions.KEYS.length; i++) {
            String key = ConstructionOptions.KEYS[i];
            ChatFormatting color = i == selectedIndex ? ChatFormatting.YELLOW : ChatFormatting.GRAY;
            Component line = Component.translatable("tetrajs.construction.option." + key).withStyle(color)
                    .append(Component.translatable("tetrajs.construction.option." + key + "." + options.getValueString(key)).withStyle(color));
            graphics.drawString(font, line, x, y, 0xFFFFFF, true);
            y += font.lineHeight;
        }
        graphics.drawString(font, Component.translatable("tetrajs.construction.hud.hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), x, y, 0xFFFFFF, true);
    }

    private static boolean isUltimineKeyDown() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options == null) {
            return false;
        }
        for (KeyMapping mapping : minecraft.options.keyMappings) {
            String name = mapping.getName();
            if (name != null && name.contains("ftbultimine") && mapping.isDown()) {
                return true;
            }
        }
        return false;
    }
}
