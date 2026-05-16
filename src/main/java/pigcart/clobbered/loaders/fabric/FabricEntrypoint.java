package pigcart.clobbered.loaders.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import pigcart.clobbered.*;
import pigcart.clobbered.networking.LobItemServerboundPayload;


public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        init();
    }

    // used also by NeoforgeEntrypoint
    public static void init() {

        PayloadTypeRegistry.serverboundPlay().register(LobItemServerboundPayload.TYPE, LobItemServerboundPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LobItemServerboundPayload.TYPE, Clobbered::handleLobItem);

        Registry.register(BuiltInRegistries.ENTITY_TYPE, Clobbered.LOBBED_ITEM_ID, Clobbered.LOBBED_ITEM);

        Clobbered.onInitialize();
    }
}