package pigcart.clobbered;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pigcart.clobbered.config.ConfigManager;
import pigcart.clobbered.networking.LobItemServerboundPayload;

import static pigcart.clobbered.config.ConfigManager.config;

public class Clobbered {

    public static final String MOD_ID = "clobbered";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, Util.getId("clobber"));

    public static final TagKey<Item> UNCATCHABLE = itemTag("uncatchable");
    public static final TagKey<Item> SOFT = itemTag("soft");
    public static final TagKey<Item> SHARP = itemTag("sharp");
    public static final TagKey<Item> SKIPPABLE = itemTag("skippable");
    public static final TagKey<Item> BLACKLISTED = itemTag("blacklisted");
    public static final TagKey<Item> EXPLODES = itemTag("explodes");
    public static final TagKey<Item> CONSUMED = itemTag("consumed");

    public static final TagKey<EntityType<?>> STICKY = entityTag("sticky");

    public static Identifier LOBBED_ITEM_ID = Util.getId("lobbed_item");
    public static EntityType<LobbedItem> LOBBED_ITEM = EntityType.Builder.<LobbedItem>of(LobbedItem::new, MobCategory.MISC)
            .clientTrackingRange(64)
            .sized(0.25f, 0.25f)
            .updateInterval(10)
            .noLootTable()
            .noSummon()
            .build(ResourceKey.create(Registries.ENTITY_TYPE, LOBBED_ITEM_ID));

    ///  sets up features that dont require a specific modloader
    public static void onInitialize() {
        ConfigManager.load();
    }

    private static TagKey<Item> itemTag(final String name) {
        return TagKey.create(Registries.ITEM, Util.getId(name));
    }

    private static TagKey<EntityType<?>> entityTag(final String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Util.getId(name));
    }

    public static void handleLobItem(LobItemServerboundPayload payload, ServerPlayNetworking.Context ctx) {
        int strength = payload.strength();
        ((PlayerDropExtension) ctx.player()).clobbered$setLobStrength(strength);
        ctx.player().drop(payload.lobAll());
    }
}