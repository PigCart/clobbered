package pigcart.clobbered.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import pigcart.clobbered.Util;

public record KickServerboundPayload() implements CustomPacketPayload {
    public static final Identifier ID = Util.getId("kick_packet");
    public static final Type<KickServerboundPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, KickServerboundPayload> CODEC = StreamCodec.unit(new KickServerboundPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
