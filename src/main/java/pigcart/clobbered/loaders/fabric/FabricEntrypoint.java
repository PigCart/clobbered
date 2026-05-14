package pigcart.clobbered.loaders.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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

        ServerPlayNetworking.registerGlobalReceiver(LobItemServerboundPayload.TYPE, (payload, ctx) -> {
            int strength = payload.strength();
            ((PlayerDropExtension)ctx.player()).clobbered$setLobStrength(strength);
            ctx.player().drop(payload.lobAll());
        });

        Identifier entityId = Util.getId("lobbed_item");

        Clobbered.LOBBED_ITEM = Registry.register(BuiltInRegistries.ENTITY_TYPE, entityId,
                EntityType.Builder.<LobbedItem>of(LobbedItem::new, MobCategory.MISC)
                        .clientTrackingRange(64)
                        .sized(0.5f, 0.5f)
                        .updateInterval(3)
                        .noLootTable()
                        .noSummon()
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, entityId))
        );

        Clobbered.onInitialize();
    }
}