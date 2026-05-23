package tschipp.carryon.networking.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.networking.PacketBase;

public record ClientboundSyncCarryDataPacket(int iden, CarryOnData data) implements PacketBase {

    public ClientboundSyncCarryDataPacket {
        if (data == null) {
            data = CarryOnData.empty();
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncCarryDataPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSyncCarryDataPacket::iden,
            CarryOnData.STREAM_CODEC, ClientboundSyncCarryDataPacket::data,
            ClientboundSyncCarryDataPacket::new
    );

    public static final CustomPacketPayload.Type<ClientboundSyncCarryDataPacket> TYPE = new Type<>(Constants.PACKET_ID_SYNC_CARRY_ON_DATA);

    @Override
    public void handle(Player player) {
        Entity e = player.level().getEntity(this.iden);
        if(e instanceof Player p) {
            p.getInventory().selected = data.getSelected();
            CarryOnDataManager.setCarryData(p, data);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
