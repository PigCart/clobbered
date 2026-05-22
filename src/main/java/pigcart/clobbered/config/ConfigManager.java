package pigcart.clobbered.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import pigcart.clobbered.Clobbered;
import pigcart.clobbered.Util;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(new TypeToken<Map<Item,Item>>(){}.getType(), new ItemMapTypeAdapter())
            .create();
    static final String CONFIG_PATH = "config/" + Clobbered.MOD_ID + ".json";
    public static ConfigData config = new ConfigData();

    public static ConfigData getDefaultConfig() {
        return new ConfigData();
    }

    public static void load() {
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, ConfigData.class);
            } catch (Exception e) {
                Clobbered.LOGGER.error("Error loading config: {}", e.getMessage());
                config = getDefaultConfig();
                save();
            }
        } else {
            Clobbered.LOGGER.info("Creating config file at " + CONFIG_PATH);
            config = getDefaultConfig();
            save();
        }
        if (config == null || config.configVersion < getDefaultConfig().configVersion) {
            Clobbered.LOGGER.info("Overwriting old config file");
            config = getDefaultConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            Clobbered.LOGGER.error("Error saving config {}", e.getMessage());
        }
    }

    public static class ColorTypeAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
        public static Color getColor(String string) {
            return Color.decode(string);
        }
        public static String getString(Color color) {
            return String.join("",
                    "#",
                    String.format("%02X", color.getRed()),
                    String.format("%02X", color.getGreen()),
                    String.format("%02X", color.getBlue()));
        }

        @Override
        public JsonElement serialize(Color color, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(getString(color));
        }
        @Override
        public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return getColor(json.getAsString());
        }
    }

    public static class ItemMapTypeAdapter implements JsonSerializer<Map<Item,Item>>, JsonDeserializer<Map<Item,Item>> {
        public static Item getItem(String string) {
            return BuiltInRegistries.ITEM.getValue(Util.parseId(string));
        }
        public static String getItemString(Item item) {
            return BuiltInRegistries.ITEM.getKey(item).toString();
        }

        @Override
        public JsonElement serialize(Map<Item,Item> map, Type type, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            map.forEach((fromItem, toItem) ->
                    object.addProperty(getItemString(fromItem), getItemString(toItem)));
            return object;
        }
        @Override
        public Map<Item,Item> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Map<Item, Item> map = new HashMap<>();
            json.getAsJsonObject().entrySet().forEach((entry) -> {
                Item fromItem = getItem(entry.getKey());
                Item toItem = getItem(entry.getValue().getAsString());
                map.put(fromItem, toItem);
            });
            return map;
        }
    }

}