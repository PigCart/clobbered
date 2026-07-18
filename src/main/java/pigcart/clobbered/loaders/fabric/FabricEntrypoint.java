//? if fabric {
package pigcart.clobbered.loaders.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import pigcart.clobbered.*;
import pigcart.clobbered.networking.KickServerboundPayload;
import pigcart.clobbered.networking.LobItemServerboundPayload;

import java.util.function.Function;


public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        init();
    }

    public static void init() {

        Registry.register(BuiltInRegistries.ITEM, Util.getId("boomerang"), Clobbered.BOOMERANG_ITEM);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register((creativeTab) -> creativeTab.accept(Clobbered.BOOMERANG_ITEM));

        FuelValueEvents.BUILD.register((builder, context) -> {
            builder.add(Clobbered.BOOMERANG_ITEM, 200);
        });

        PayloadTypeRegistry.serverboundPlay().register(LobItemServerboundPayload.TYPE, LobItemServerboundPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(KickServerboundPayload.TYPE, KickServerboundPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LobItemServerboundPayload.TYPE,
                (payload, ctx) -> Clobbered.handleLobItem(payload, ctx.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(KickServerboundPayload.TYPE,
                (payload, ctx) -> Clobbered.handleKick(payload, ctx.player())
        );

        Registry.register(BuiltInRegistries.ENTITY_TYPE, Clobbered.LOBBED_ITEM_ID, Clobbered.LOBBED_ITEM);

        Clobbered.onInitialize();
    }
}
//?}