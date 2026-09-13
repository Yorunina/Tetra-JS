package net.yiran.tetrajs.construction;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yiran.tetrajs.TetraJS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = TetraJS.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ConstructionUndoHistory {
    private static final int HISTORY_LIMIT = 3;
    private static final Map<UUID, PlayerEntry> HISTORY = new HashMap<>();

    private ConstructionUndoHistory() {
    }

    public static void add(Player player, Level level, List<ConstructionJob.PlacedBlock> placed) {
        PlayerEntry playerEntry = entry(player);
        LinkedList<HistoryEntry> entries = playerEntry.entries;
        entries.add(new HistoryEntry(new ArrayList<>(placed), level));
        while (entries.size() > HISTORY_LIMIT) {
            entries.removeFirst();
        }
        if (playerEntry.undoActive) {
            updateClient(player, true);
        }
    }

    public static void updateClient(Player player, boolean undoActive) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        PlayerEntry playerEntry = entry(player);
        playerEntry.undoActive = undoActive;
        Set<BlockPos> positions = Collections.emptySet();
        if (!playerEntry.entries.isEmpty()) {
            HistoryEntry last = playerEntry.entries.getLast();
            if (last.level.equals(player.level())) {
                positions = last.positions();
            }
        }
        ConstructionNetwork.sendUndoBlocks(serverPlayer, positions);
    }

    public static boolean isUndoActive(Player player) {
        return entry(player).undoActive;
    }

    public static boolean undo(Player player, Level level, BlockPos pos) {
        PlayerEntry playerEntry = entry(player);
        if (!playerEntry.undoActive || playerEntry.entries.isEmpty()) {
            return false;
        }
        HistoryEntry last = playerEntry.entries.getLast();
        if (!last.level.equals(level) || !last.withinRange(pos)) {
            return false;
        }
        if (!last.restore(player)) {
            return false;
        }
        playerEntry.entries.removeLast();
        updateClient(player, true);
        return true;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HISTORY.remove(event.getEntity().getUUID());
    }

    private static PlayerEntry entry(Player player) {
        return HISTORY.computeIfAbsent(player.getUUID(), key -> new PlayerEntry());
    }

    private static final class PlayerEntry {
        private final LinkedList<HistoryEntry> entries = new LinkedList<>();
        private boolean undoActive;
    }

    private static final class HistoryEntry {
        private final List<ConstructionJob.PlacedBlock> placed;
        private final Level level;

        private HistoryEntry(List<ConstructionJob.PlacedBlock> placed, Level level) {
            this.placed = placed;
            this.level = level;
        }

        private Set<BlockPos> positions() {
            return placed.stream().map(ConstructionJob.PlacedBlock::pos).collect(Collectors.toSet());
        }

        private boolean withinRange(BlockPos pos) {
            for (ConstructionJob.PlacedBlock block : placed) {
                if (pos.closerThan(block.pos(), 3)) {
                    return true;
                }
            }
            return false;
        }

        private boolean restore(Player player) {
            for (ConstructionJob.PlacedBlock block : placed) {
                if (!ConstructionJob.canRestore(level, player, block)) {
                    return false;
                }
            }
            for (ConstructionJob.PlacedBlock block : placed) {
                if (ConstructionJob.restore(level, player, block) && !player.getAbilities().instabuild) {
                    ItemStack stack = new ItemStack(block.item());
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }
            player.getInventory().setChanged();
            level.playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            return true;
        }
    }
}
