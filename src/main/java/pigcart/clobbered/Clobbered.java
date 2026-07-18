package pigcart.clobbered;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.phys.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pigcart.clobbered.config.ConfigManager;
import pigcart.clobbered.networking.KickServerboundPayload;
import pigcart.clobbered.networking.LobItemServerboundPayload;

import java.util.List;

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
    public static final TagKey<Item> BOOMERANG = itemTag("boomerang");

    public static final TagKey<EntityType<?>> STICKY = entityTag("sticky");

    public static Identifier LOBBED_ITEM_ID = Util.getId("lobbed_item");
    public static EntityType<LobbedItem> LOBBED_ITEM = EntityType.Builder.<LobbedItem>of(LobbedItem::new, MobCategory.MISC)
            .clientTrackingRange(64)
            .sized(0.25f, 0.25f)
            .updateInterval(2)
            .noLootTable()
            .noSummon()
            .build(ResourceKey.create(Registries.ENTITY_TYPE, LOBBED_ITEM_ID));

    public static Item BOOMERANG_ITEM = new Item(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, Util.getId("boomerang")))
            .stacksTo(1)
            //.tool(ToolMaterial.WOOD, Tags.Blocks.BUDS, 2.5F, -2.4F, 0.5F)
            .sword(ToolMaterial.WOOD, 2.5F, -2.4F)
    );

    public static void onInitialize() {
        ConfigManager.load();
    }

    private static TagKey<Item> itemTag(final String name) {
        return TagKey.create(Registries.ITEM, Util.getId(name));
    }

    private static TagKey<EntityType<?>> entityTag(final String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Util.getId(name));
    }

    public static void handleLobItem(LobItemServerboundPayload payload, ServerPlayer player) {
        int strength = payload.strength();
        ((PlayerDropExtension) player).clobbered$setLobStrength(strength);
        player.drop(payload.lobAll());
    }

    public static void handleKick(KickServerboundPayload payload, ServerPlayer player) {
        Vec3 forward = player.getForward();
        Vec3 position = player.position();
        Vec3 kickPos = position.add(forward.x * 2, 0, forward.z * 2);
        final List<Entity> kicked = player.level().getEntitiesOfClass(Entity.class,
                AABB.ofSize(kickPos, 1, 1, 1)
        );
        for (Entity entity : kicked) {
            if (entity instanceof LobbedItem lobbedItem && lobbedItem.isImpaling()) continue;
            entity.addDeltaMovement(new Vec3(forward.x, 0.1, forward.z));
        }
    }
}