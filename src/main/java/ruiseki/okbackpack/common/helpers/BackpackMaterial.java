package ruiseki.okbackpack.common.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.Setter;
import ruiseki.okcore.helper.JsonNBTHelpers;
import ruiseki.okcore.json.AbstractJsonMaterial;
import ruiseki.okcore.json.ItemJson;

/**
 * Material class representing a Backpack template JSON.
 */
public class BackpackMaterial extends AbstractJsonMaterial {

    @Getter
    @Setter
    private String backpackTier;
    @Getter
    @Setter
    private String mainColor = "#FFFFFF";
    @Getter
    @Setter
    private String accentColor = "#FFFFFF";
    @Getter
    private final List<BackpackEntry> inventory = new ArrayList<>();
    @Getter
    private final List<BackpackEntry> upgrade = new ArrayList<>();
    @Getter
    @Setter
    private boolean searchBackpack = true;

    @Override
    public void read(JsonObject json) {
        this.backpackTier = getString(json, "BackpackTier", "Leather");
        this.mainColor = getString(json, "MainColor", "#FFFFFF");
        this.accentColor = getString(json, "AccentColor", "#FFFFFF");
        this.searchBackpack = getBoolean(json, "SearchBackpack", true);

        // Inventory
        this.inventory.clear();
        if (json.has("Inventory") && json.get("Inventory")
            .isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray("Inventory")) {
                if (e.isJsonObject()) {
                    BackpackEntry entry = new BackpackEntry();
                    entry.read(e.getAsJsonObject());
                    this.inventory.add(entry);
                }
            }
        }

        // Upgrade
        this.upgrade.clear();
        if (json.has("Upgrade") && json.get("Upgrade")
            .isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray("Upgrade")) {
                if (e.isJsonObject()) {
                    BackpackEntry entry = new BackpackEntry();
                    entry.read(e.getAsJsonObject());
                    this.upgrade.add(entry);
                }
            }
        }

        captureUnknownProperties(
            json,
            "BackpackTier",
            "MainColor",
            "AccentColor",
            "Inventory",
            "Upgrade",
            "SearchBackpack");
    }

    @Override
    public void write(JsonObject json) {
        json.addProperty("BackpackTier", backpackTier);
        json.addProperty("MainColor", mainColor);
        json.addProperty("AccentColor", accentColor);
        json.addProperty("SearchBackpack", searchBackpack);

        if (!inventory.isEmpty()) {
            JsonArray arr = new JsonArray();
            for (BackpackEntry entry : inventory) {
                JsonObject obj = new JsonObject();
                entry.write(obj);
                arr.add(obj);
            }
            json.add("Inventory", arr);
        }

        if (!upgrade.isEmpty()) {
            JsonArray arr = new JsonArray();
            for (BackpackEntry entry : upgrade) {
                JsonObject obj = new JsonObject();
                entry.write(obj);
                arr.add(obj);
            }
            json.add("Upgrade", arr);
        }

        writeUnknownProperties(json);
    }

    @Override
    public boolean validate() {
        if (backpackTier == null) {
            logValidationError("BackpackTier is missing");
            return false;
        }
        return true;
    }

    public static class BackpackEntry {

        public int slot;
        public String id;
        public int count = 1;
        public NBTTagCompound nbt;

        public void read(JsonObject json) {
            this.slot = json.has("Slot") ? json.get("Slot")
                .getAsInt() : 0;
            this.id = json.has("id") ? json.get("id")
                .getAsString() : null;
            this.count = json.has("Count") ? json.get("Count")
                .getAsInt() : 1;

            if (json.has("nbt") && json.get("nbt")
                .isJsonObject()) {
                this.nbt = JsonNBTHelpers.jsonToNBT(json.getAsJsonObject("nbt"));
            }
        }

        public void write(JsonObject json) {
            json.addProperty("Slot", slot);
            if (id != null) json.addProperty("id", id);
            if (count != 1) json.addProperty("Count", count);
            if (nbt != null) {
                json.add("nbt", JsonNBTHelpers.nbtToJSON(nbt));
            }
        }

        public ItemStack toItemStack() {
            ItemJson itemJson = new ItemJson();
            itemJson.name = id;
            itemJson.amount = count;

            ItemStack stack = ItemJson.resolveItemStack(itemJson);
            if (stack == null) return null;
            if (nbt != null) {
                stack.setTagCompound((NBTTagCompound) nbt.copy());
            }
            return stack;
        }

        public static BackpackEntry fromItemStack(int slot, ItemStack stack) {
            if (stack == null) return null;
            BackpackEntry entry = new BackpackEntry();
            entry.slot = slot;
            ItemJson itemJson = ItemJson.parseItemStack(stack);
            if (itemJson != null) {
                // Check if it has metadata in ItemJson
                if (itemJson.meta != 0) {
                    entry.id = itemJson.name + ":" + itemJson.meta;
                } else {
                    entry.id = itemJson.name;
                }
            }
            entry.count = stack.stackSize;
            if (stack.hasTagCompound()) {
                entry.nbt = (NBTTagCompound) stack.getTagCompound()
                    .copy();
            }
            return entry;
        }
    }

    public int parseMainColor() {
        return parseHexColor(mainColor);
    }

    public int parseAccentColor() {
        return parseHexColor(accentColor);
    }

    private int parseHexColor(String hex) {
        if (hex == null || !hex.startsWith("#")) return 0xFFFFFF;
        try {
            return Integer.parseInt(hex.substring(1), 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    public static String toHexColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
