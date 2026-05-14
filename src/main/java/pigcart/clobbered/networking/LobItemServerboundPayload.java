package pigcart.clobbered.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import pigcart.clobbered.Util;

public record LobItemServerboundPayload(int strength, boolean lobAll) implements CustomPacketPayload {
    public static final Identifier ID = Util.getId("lob_packet");
    public static final CustomPacketPayload.Type<LobItemServerboundPayload> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, LobItemServerboundPayload> CODEC = StreamCodec.composite(ByteBufCodecs.INT, LobItemServerboundPayload::strength, ByteBufCodecs.BOOL, LobItemServerboundPayload::lobAll, LobItemServerboundPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
