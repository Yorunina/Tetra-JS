package net.yiran.tetrajs.construction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.yiran.tetrajs.TetraJS;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class ConstructionNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TetraJS.MODID, "construction"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private ConstructionNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, OptionPacket.class, OptionPacket::encode, OptionPacket::decode, OptionPacket::handle);
        CHANNEL.registerMessage(id++, QueryUndoPacket.class, QueryUndoPacket::encode, QueryUndoPacket::decode, QueryUndoPacket::handle);
        CHANNEL.registerMessage(id, UndoBlocksPacket.class, UndoBlocksPacket::encode, UndoBlocksPacket::decode, UndoBlocksPacket::handle);
    }

    public static void sendOption(String key, String value) {
        CHANNEL.sendToServer(new OptionPacket(key, value));
    }

    public static void sendQueryUndo(boolean undoActive) {
        CHANNEL.sendToServer(new QueryUndoPacket(undoActive));
    }

    public static void sendUndoBlocks(ServerPlayer player, Set<BlockPos> positions) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UndoBlocksPacket(positions));
    }

    public record OptionPacket(String key, String value) {
        private static void encode(OptionPacket packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.key);
            buffer.writeUtf(packet.value);
        }

        private static OptionPacket decode(FriendlyByteBuf buffer) {
            return new OptionPacket(buffer.readUtf(100), buffer.readUtf(100));
        }

        private static void handle(OptionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }
                ItemStack stack = player.getMainHandItem();
                if (!ConstructionJob.hasEffect(stack)) {
                    return;
                }
                ConstructionOptions options = new ConstructionOptions(stack);
                if (!options.set(packet.key, packet.value)) {
                    return;
                }
                player.getInventory().setChanged();
            });
            context.setPacketHandled(true);
        }
    }

    public record QueryUndoPacket(boolean undoActive) {
        private static void encode(QueryUndoPacket packet, FriendlyByteBuf buffer) {
            buffer.writeBoolean(packet.undoActive);
        }

        private static QueryUndoPacket decode(FriendlyByteBuf buffer) {
            return new QueryUndoPacket(buffer.readBoolean());
        }

        private static void handle(QueryUndoPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    ConstructionUndoHistory.updateClient(player, packet.undoActive);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record UndoBlocksPacket(Set<BlockPos> positions) {
        private static void encode(UndoBlocksPacket packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.positions.size());
            for (BlockPos pos : packet.positions) {
                buffer.writeBlockPos(pos);
            }
        }

        private static UndoBlocksPacket decode(FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            HashSet<BlockPos> positions = new HashSet<>();
            for (int i = 0; i < size; i++) {
                positions.add(buffer.readBlockPos());
            }
            return new UndoBlocksPacket(positions);
        }

        private static void handle(UndoBlocksPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ConstructionClientEvents.setUndoBlocks(packet.positions)));
            context.setPacketHandled(true);
        }
    }
}
