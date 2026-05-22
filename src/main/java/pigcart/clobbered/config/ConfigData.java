package pigcart.clobbered.config;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import pigcart.clobbered.config.gui.Annotations.*;

import java.util.HashMap;
import java.util.Map;


public class ConfigData {
    @NoGUI public byte configVersion = 0;

    public int minimumDropStrength = 8;
    public int maximumDropStrength = 20;
    public boolean showStrengthBar = true;
    public float interactionRadius = 0.3F;
    public float damageScalingPower = 0.2F;
    public boolean automaticItemPickUp = false;
    public boolean automaticArrowPickUp = false;

    public float itemBreakChance = 0.1F;
    public Map<Item, Item> brokenItems = new HashMap<>(Map.ofEntries(
            Map.entry(Items.STONE_BRICKS, Items.CRACKED_STONE_BRICKS),
            Map.entry(Items.DEEPSLATE_BRICKS, Items.CRACKED_DEEPSLATE_BRICKS),
            Map.entry(Items.DEEPSLATE_TILES, Items.CRACKED_DEEPSLATE_TILES),
            Map.entry(Items.NETHER_BRICKS, Items.CRACKED_NETHER_BRICKS),
            Map.entry(Items.POLISHED_BLACKSTONE_BRICKS, Items.CRACKED_POLISHED_BLACKSTONE_BRICKS),
            Map.entry(Items.STONE, Items.COBBLESTONE),
            Map.entry(Items.DEEPSLATE, Items.COBBLED_DEEPSLATE),
            Map.entry(Items.ANVIL, Items.CHIPPED_ANVIL),
            Map.entry(Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL),
            Map.entry(Items.PRISMARINE, Items.PRISMARINE_SHARD),
            Map.entry(Items.SEA_LANTERN, Items.PRISMARINE_CRYSTALS),
            Map.entry(Items.TORCH, Items.CHARCOAL),
            Map.entry(Items.DECORATED_POT, Items.BRICK),
            Map.entry(Items.FLOWER_POT, Items.BRICK),
            Map.entry(Items.POLISHED_BLACKSTONE, Items.BLACKSTONE),
            Map.entry(Items.POLISHED_ANDESITE, Items.ANDESITE),
            Map.entry(Items.POLISHED_DIORITE, Items.DIORITE),
            Map.entry(Items.POLISHED_GRANITE, Items.GRANITE),
            Map.entry(Items.POLISHED_BASALT, Items.BASALT),
            Map.entry(Items.POLISHED_DEEPSLATE, Items.DEEPSLATE),
            Map.entry(Items.POLISHED_TUFF, Items.TUFF)
    ));
}