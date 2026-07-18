package pigcart.clobbered;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import pigcart.clobbered.config.gui.ConfigScreen;
import pigcart.clobbered.networking.KickServerboundPayload;

import java.util.List;

public class ClobberedClient {

    public static int throwStrength = 0;

    @SuppressWarnings("unchecked")
    public static <S> LiteralArgumentBuilder<S> getCommands() {
        return (LiteralArgumentBuilder<S>) LiteralArgumentBuilder.literal(Clobbered.MOD_ID)
                .executes(ctx -> {
                    // give minecraft a tick to close the chat screen
                    Minecraft.getInstance().schedule(() -> Minecraft.getInstance().setScreen(ConfigScreen.get(null)));
                    return 0;
                });
    }
}