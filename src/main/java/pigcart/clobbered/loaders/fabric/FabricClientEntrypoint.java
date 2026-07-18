//? if fabric {
package pigcart.clobbered.loaders.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import org.lwjgl.glfw.GLFW;
import pigcart.clobbered.*;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import pigcart.clobbered.networking.KickServerboundPayload;


import static pigcart.clobbered.ClobberedClient.getDebugLines;


public class FabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        init();
    }

    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(ClobberedClient::onTick);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClobberedClient.getCommands());
        });

        EntityRenderers.register(Clobbered.LOBBED_ITEM, LobbedItemRenderer::new);

        KeyMapping kickKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.clobbered.kick",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_C,
                        KeyMapping.Category.register(Util.getId("keybinds"))
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (kickKey.consumeClick()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new KickServerboundPayload());
                }
            }
        });
    }
}
//?}