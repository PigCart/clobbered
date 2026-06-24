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
import pigcart.clobbered.networking.LobItemServerboundPayload;

import java.util.function.Function;


public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        init();
    }

    // used also by NeoforgeEntrypoint
    public static void init() {

        Clobbered.BOOMERANG_ITEM = registerItem("boomerang", Item::new, new Item.Properties());

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register((creativeTab) -> creativeTab.accept(Clobbered.BOOMERANG_ITEM));

        FuelValueEvents.BUILD.register((builder, context) -> {
            builder.add(Clobbered.BOOMERANG_ITEM, 200);
        });

        PayloadTypeRegistry.serverboundPlay().register(LobItemServerboundPayload.TYPE, LobItemServerboundPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LobItemServerboundPayload.TYPE, Clobbered::handleLobItem);

        Registry.register(BuiltInRegistries.ENTITY_TYPE, Clobbered.LOBBED_ITEM_ID, Clobbered.LOBBED_ITEM);

        Clobbered.onInitialize();
    }

    public static <T extends Item> T registerItem(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Util.getId(name));
        T item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }
}