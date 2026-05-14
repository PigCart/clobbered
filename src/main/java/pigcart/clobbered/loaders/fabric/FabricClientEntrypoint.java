package pigcart.clobbered.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.renderer.entity.EntityRenderers;
import pigcart.clobbered.*;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;


import static pigcart.clobbered.ClobberedClient.getDebugLines;


public class FabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        init();
    }

    // used also by NeoforgeEntrypoint
    public static void init() {

        DebugScreenEntries.register( Util.getId("debug"),
                (display, level, levelChunk, levelChunk2) ->
                        display.addToGroup(Util.getId("debuglines"), getDebugLines())
        );

        ClientTickEvents.END_CLIENT_TICK.register(ClobberedClient::onTick);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClobberedClient.getCommands());
        });

        EntityRenderers.register(Clobbered.LOBBED_ITEM, LobbedItemRenderer::new);
    }
}