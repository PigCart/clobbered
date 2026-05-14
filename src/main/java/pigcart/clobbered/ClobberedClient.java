package pigcart.clobbered;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import pigcart.clobbered.config.gui.ConfigScreen;

import java.util.List;

public class ClobberedClient {

    public static List<String> getDebugLines() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return List.of("no level loaded :3");
        return List.of(
                "wow look, nothing!"
        );
    }

    @SuppressWarnings("unchecked")
    public static <S> LiteralArgumentBuilder<S> getCommands() {
        return (LiteralArgumentBuilder<S>) LiteralArgumentBuilder.literal(Clobbered.MOD_ID)
                .executes(ctx -> {
                    // give minecraft a tick to close the chat screen
                    Minecraft.getInstance().schedule(() -> Minecraft.getInstance().setScreen(ConfigScreen.get(null)));
                    return 0;
                })
                .then(LiteralArgumentBuilder.literal("debug")
                        .executes(ctx -> {
                            getDebugLines().forEach(Util::addChatMsg);
                            return 0;
                        })
                );
    }

    public static void onTick(Minecraft client) {
        if (!client.isPaused() && client.level != null && client.getCameraEntity() != null) {

        }
    }
}